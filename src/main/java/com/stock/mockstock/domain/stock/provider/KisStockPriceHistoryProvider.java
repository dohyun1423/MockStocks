package com.stock.mockstock.domain.stock.provider;

import com.stock.mockstock.domain.stock.dto.StockPriceHistoryResponse;
import com.stock.mockstock.domain.stock.dto.kis.KisDailyChartResponse;
import com.stock.mockstock.domain.stock.dto.kis.KisMinuteChartResponse;
import com.stock.mockstock.domain.stock.kis.KisTokenService;
import com.stock.mockstock.global.config.KisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kis.provider", havingValue = "kis")
public class KisStockPriceHistoryProvider implements StockPriceHistoryProvider {

    private static final String DAILY_CHART_PATH = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";
    private static final String MINUTE_CHART_PATH = "/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice";
    private static final String DAILY_MINUTE_CHART_PATH = "/uapi/domestic-stock/v1/quotations/inquire-time-dailychartprice";

    private static final String DOMESTIC_STOCK_MARKET_CODE = "J";
    private static final String DAILY_CHART_TR_ID = "FHKST03010100";
    private static final String MINUTE_CHART_TR_ID = "FHKST03010200";
    private static final String DAILY_MINUTE_CHART_TR_ID = "FHKST03010230";

    private static final String ORG_ADJUST_PRICE = "0";
    private static final String INCLUDE_PAST_DATA = "Y";
    private static final String EXCLUDE_FAKE_TICK = "N";
    private static final String ETC_CLASS_CODE = "";

    // 1D 초기 차트는 당일 분봉 API를 여러 번 호출해 장중 데이터를 구성한다.
    private static final int MAX_MINUTE_REQUEST_COUNT = 10;

    // 1W 차트는 날짜별 분봉 API를 하루 최대 4번 호출해 최근 1주 분봉 데이터를 구성한다.
    private static final int MAX_DAILY_MINUTE_REQUEST_COUNT = 3;

    // 1W 차트는 1분봉을 10분 단위로 묶어 차트 렌더링 부담을 줄인다.
    private static final int WEEK_MINUTE_BUCKET_UNIT = 10;

    private static final DateTimeFormatter KIS_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter KIS_TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    private final KisProperties kisProperties;
    private final KisTokenService kisTokenService;
    private final RestClient.Builder restClientBuilder;

    // 1D는 당일 분봉, 1W는 실전 일별분봉, 1M/1Y는 일봉으로 조회한다.
    @Override
    public List<StockPriceHistoryResponse> getPriceHistories(String symbol, String period) {
        String normalizedSymbol = normalizeSymbol(symbol);
        String normalizedPeriod = normalizePeriod(period);

        if ("1D".equals(normalizedPeriod)) {
            return getTodayMinuteHistories(normalizedSymbol);
        }

        if ("1W".equals(normalizedPeriod)) {
            return getLastWeekMinuteHistories(normalizedSymbol);
        }

        return getDailyHistories(normalizedSymbol, normalizedPeriod);
    }

    // KIS 주식당일분봉조회 API를 여러 번 호출해 오늘 장중 1분봉 데이터를 구성한다.
    private List<StockPriceHistoryResponse> getTodayMinuteHistories(String symbol) {
        Map<String, StockPriceHistoryResponse> historiesByLabel = new LinkedHashMap<>();
        LocalTime cursorTime = getMinuteChartEndTime();

        int executedRequestCount = 0;

        log.info(
                "KIS minute chart provider used. symbol={}, endTime={}, maxRequestCount={}",
                symbol,
                cursorTime,
                MAX_MINUTE_REQUEST_COUNT
        );

        for (int requestCount = 0; requestCount < MAX_MINUTE_REQUEST_COUNT; requestCount++) {
            KisMinuteChartResponse response = requestMinuteChart(symbol, cursorTime);
            executedRequestCount++;

            validateMinuteResponse(response);

            List<KisMinuteChartResponse.Output> outputs = response.getOutput2();

            if (outputs.isEmpty()) {
                break;
            }

            outputs.stream()
                    .map(this::toMinuteResponse)
                    .forEach((history) -> historiesByLabel.put(history.getLabel(), history));

            LocalTime earliestTime = outputs.stream()
                    .map(KisMinuteChartResponse.Output::getTradeTime)
                    .filter((time) -> time != null && time.length() == 6)
                    .map(this::parseKisTime)
                    .min(LocalTime::compareTo)
                    .orElse(LocalTime.of(9, 0));

            if (!earliestTime.isAfter(LocalTime.of(9, 0))) {
                break;
            }

            cursorTime = earliestTime.minusMinutes(1);
        }

        List<StockPriceHistoryResponse> histories = historiesByLabel.values()
                .stream()
                .sorted(Comparator.comparing(StockPriceHistoryResponse::getLabel))
                .toList();

        log.info(
                "KIS minute chart loaded. symbol={}, requestCount={}, pointCount={}",
                symbol,
                executedRequestCount,
                histories.size()
        );

        return histories;
    }

    // KIS 실전 주식일별분봉조회 API로 현재 날짜 기준 최근 1주 분봉 데이터를 조회한다.
    private List<StockPriceHistoryResponse> getLastWeekMinuteHistories(String symbol) {
        Map<String, StockPriceHistoryResponse> historiesByLabel = new LinkedHashMap<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        int executedRequestCount = 0;

        log.info(
                "KIS daily minute chart provider used. symbol={}, startDate={}, endDate={}, maxRequestPerDay={}",
                symbol,
                startDate,
                endDate,
                MAX_DAILY_MINUTE_REQUEST_COUNT
        );

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalTime cursorTime = getDailyMinuteCursorTime(date, endDate);

            for (int requestCount = 0; requestCount < MAX_DAILY_MINUTE_REQUEST_COUNT; requestCount++) {
                KisMinuteChartResponse response = requestDailyMinuteChart(symbol, date, cursorTime);
                executedRequestCount++;

                validateMinuteResponse(response);

                List<KisMinuteChartResponse.Output> outputs = response.getOutput2();

                if (outputs.isEmpty()) {
                    break;
                }

                outputs.stream()
                        .map(this::toMinuteResponse)
                        .forEach((history) -> historiesByLabel.put(history.getLabel(), history));

                LocalTime earliestTime = outputs.stream()
                        .map(KisMinuteChartResponse.Output::getTradeTime)
                        .filter((time) -> time != null && time.length() == 6)
                        .map(this::parseKisTime)
                        .min(LocalTime::compareTo)
                        .orElse(LocalTime.of(9, 0));

                if (!earliestTime.isAfter(LocalTime.of(9, 0))) {
                    break;
                }

                cursorTime = earliestTime.minusMinutes(1);
            }
        }

        List<StockPriceHistoryResponse> histories = historiesByLabel.values()
                .stream()
                .sorted(Comparator.comparing(StockPriceHistoryResponse::getLabel))
                .toList();

        List<StockPriceHistoryResponse> compactedHistories =
                compactMinuteHistories(histories, WEEK_MINUTE_BUCKET_UNIT);

        log.info(
                "KIS daily minute chart loaded. symbol={}, requestCount={}, pointCount={}, compactedPointCount={}",
                symbol,
                executedRequestCount,
                histories.size(),
                compactedHistories.size()
        );

        return compactedHistories;
    }

    // 1분봉 데이터를 지정 분 단위로 묶어 차트 렌더링 부담을 줄인다.
    private List<StockPriceHistoryResponse> compactMinuteHistories(
            List<StockPriceHistoryResponse> histories,
            int minuteUnit
    ) {
        Map<String, List<StockPriceHistoryResponse>> groupedHistories = histories.stream()
                .collect(Collectors.groupingBy(
                        (history) -> createMinuteBucketLabel(history.getLabel(), minuteUnit),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return groupedHistories.entrySet()
                .stream()
                .map((entry) -> toCompactedMinuteHistory(entry.getKey(), entry.getValue()))
                .toList();
    }

    // yyyy-MM-dd HH:mm 라벨을 yyyy-MM-dd HH:10처럼 지정 분 단위 버킷으로 바꾼다.
    private String createMinuteBucketLabel(String label, int minuteUnit) {
        LocalDateTime dateTime = LocalDateTime.parse(label, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        int bucketMinute = (dateTime.getMinute() / minuteUnit) * minuteUnit;

        return dateTime.withMinute(bucketMinute)
                .withSecond(0)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    // 같은 시간 버킷에 속한 분봉들을 하나의 OHLCV 데이터로 합친다.
    private StockPriceHistoryResponse toCompactedMinuteHistory(
            String label,
            List<StockPriceHistoryResponse> histories
    ) {
        StockPriceHistoryResponse first = histories.get(0);
        StockPriceHistoryResponse last = histories.get(histories.size() - 1);

        long highPrice = histories.stream()
                .mapToLong(StockPriceHistoryResponse::getHighPrice)
                .max()
                .orElse(last.getClosePrice());

        long lowPrice = histories.stream()
                .mapToLong(StockPriceHistoryResponse::getLowPrice)
                .min()
                .orElse(last.getClosePrice());

        long volume = histories.stream()
                .mapToLong(StockPriceHistoryResponse::getVolume)
                .sum();

        return new StockPriceHistoryResponse(
                label,
                first.getOpenPrice(),
                highPrice,
                lowPrice,
                last.getClosePrice(),
                volume
        );
    }

    // KIS 기간별 시세 API로 1M, 1Y 차트 데이터를 일봉 기준으로 조회한다.
    private List<StockPriceHistoryResponse> getDailyHistories(String symbol, String period) {
        String periodCode = toKisPeriodCode(period);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(endDate, period);

        log.info(
                "KIS chart provider used. symbol={}, uiPeriod={}, kisPeriod={}, startDate={}, endDate={}",
                symbol,
                period,
                periodCode,
                startDate,
                endDate
        );

        KisDailyChartResponse response = restClientBuilder
                .baseUrl(kisProperties.getBaseUrl())
                .build()
                .get()
                .uri((uriBuilder) -> uriBuilder
                        .path(DAILY_CHART_PATH)
                        .queryParam("FID_COND_MRKT_DIV_CODE", DOMESTIC_STOCK_MARKET_CODE)
                        .queryParam("FID_INPUT_ISCD", symbol)
                        .queryParam("FID_INPUT_DATE_1", startDate.format(KIS_DATE_FORMATTER))
                        .queryParam("FID_INPUT_DATE_2", endDate.format(KIS_DATE_FORMATTER))
                        .queryParam("FID_PERIOD_DIV_CODE", periodCode)
                        .queryParam("FID_ORG_ADJ_PRC", ORG_ADJUST_PRICE)
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + kisTokenService.getAccessToken())
                .header("appkey", kisProperties.getAppKey())
                .header("appsecret", kisProperties.getAppSecret())
                .header("tr_id", DAILY_CHART_TR_ID)
                .header("custtype", "P")
                .retrieve()
                .body(KisDailyChartResponse.class);

        validateDailyResponse(response);

        return response.getOutput2()
                .stream()
                .map(this::toDailyResponse)
                .sorted(Comparator.comparing(StockPriceHistoryResponse::getLabel))
                .toList();
    }

    // 지정 시간 기준으로 KIS 당일 분봉 최대 30건을 조회한다.
    private KisMinuteChartResponse requestMinuteChart(String symbol, LocalTime inputTime) {
        return restClientBuilder
                .baseUrl(kisProperties.getBaseUrl())
                .build()
                .get()
                .uri((uriBuilder) -> uriBuilder
                        .path(MINUTE_CHART_PATH)
                        .queryParam("FID_COND_MRKT_DIV_CODE", DOMESTIC_STOCK_MARKET_CODE)
                        .queryParam("FID_INPUT_ISCD", symbol)
                        .queryParam("FID_INPUT_HOUR_1", inputTime.format(KIS_TIME_FORMATTER))
                        .queryParam("FID_PW_DATA_INCU_YN", INCLUDE_PAST_DATA)
                        .queryParam("FID_ETC_CLS_CODE", ETC_CLASS_CODE)
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + kisTokenService.getAccessToken())
                .header("appkey", kisProperties.getAppKey())
                .header("appsecret", kisProperties.getAppSecret())
                .header("tr_id", MINUTE_CHART_TR_ID)
                .header("custtype", "P")
                .retrieve()
                .body(KisMinuteChartResponse.class);
    }

    // 지정 날짜와 시간 기준으로 KIS 실전 일별분봉 최대 120건을 조회한다.
    private KisMinuteChartResponse requestDailyMinuteChart(String symbol, LocalDate date, LocalTime inputTime) {
        return restClientBuilder
                .baseUrl(kisProperties.getBaseUrl())
                .build()
                .get()
                .uri((uriBuilder) -> uriBuilder
                        .path(DAILY_MINUTE_CHART_PATH)
                        .queryParam("FID_COND_MRKT_DIV_CODE", DOMESTIC_STOCK_MARKET_CODE)
                        .queryParam("FID_INPUT_ISCD", symbol)
                        .queryParam("FID_INPUT_DATE_1", date.format(KIS_DATE_FORMATTER))
                        .queryParam("FID_INPUT_HOUR_1", inputTime.format(KIS_TIME_FORMATTER))
                        .queryParam("FID_PW_DATA_INCU_YN", INCLUDE_PAST_DATA)
                        .queryParam("FID_FAKE_TICK_INCU_YN", EXCLUDE_FAKE_TICK)
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + kisTokenService.getAccessToken())
                .header("appkey", kisProperties.getAppKey())
                .header("appsecret", kisProperties.getAppSecret())
                .header("tr_id", DAILY_MINUTE_CHART_TR_ID)
                .header("custtype", "P")
                .retrieve()
                .body(KisMinuteChartResponse.class);
    }

    private StockPriceHistoryResponse toDailyResponse(KisDailyChartResponse.Output output) {
        return new StockPriceHistoryResponse(
                formatDateLabel(output.getBusinessDate()),
                parseLong(output.getOpenPrice()),
                parseLong(output.getHighPrice()),
                parseLong(output.getLowPrice()),
                parseLong(output.getClosePrice()),
                parseLong(output.getAccumulatedVolume())
        );
    }

    private StockPriceHistoryResponse toMinuteResponse(KisMinuteChartResponse.Output output) {
        return new StockPriceHistoryResponse(
                formatMinuteLabel(output.getBusinessDate(), output.getTradeTime()),
                parseLong(output.getOpenPrice()),
                parseLong(output.getHighPrice()),
                parseLong(output.getLowPrice()),
                parseLong(output.getClosePrice()),
                parseLong(output.getVolume())
        );
    }

    private void validateDailyResponse(KisDailyChartResponse response) {
        if (response == null || response.getOutput2() == null) {
            throw new IllegalStateException("KIS daily chart response is empty.");
        }

        if (!"0".equals(response.getRtCd())) {
            throw new IllegalStateException("KIS daily chart request failed. message=" + response.getMsg1());
        }
    }

    private void validateMinuteResponse(KisMinuteChartResponse response) {
        if (response == null || response.getOutput2() == null) {
            throw new IllegalStateException("KIS minute chart response is empty.");
        }

        if (!"0".equals(response.getRtCd())) {
            throw new IllegalStateException("KIS minute chart request failed. message=" + response.getMsg1());
        }
    }

    private LocalTime getMinuteChartEndTime() {
        LocalTime now = LocalTime.now();
        LocalTime marketOpen = LocalTime.of(9, 0);
        LocalTime marketClose = LocalTime.of(15, 30);

        if (now.isBefore(marketOpen)) {
            return marketClose;
        }

        if (now.isAfter(marketClose)) {
            return marketClose;
        }

        return now;
    }

    // 과거 날짜는 장마감 기준, 오늘은 현재 장중 시간 기준으로 분봉 조회 시작 시간을 정한다.
    private LocalTime getDailyMinuteCursorTime(LocalDate date, LocalDate endDate) {
        if (date.equals(endDate)) {
            return getMinuteChartEndTime();
        }

        return LocalTime.of(15, 30);
    }

    private LocalTime parseKisTime(String value) {
        return LocalTime.parse(value, KIS_TIME_FORMATTER);
    }

    private String normalizeSymbol(String symbol) {
        return String.valueOf(symbol)
                .trim()
                .replaceAll("\\s+", "")
                .toUpperCase();
    }

    private String normalizePeriod(String period) {
        if (period == null || period.isBlank()) {
            return "1M";
        }

        return switch (period.toUpperCase()) {
            case "1D", "1W", "1M", "1Y" -> period.toUpperCase();
            default -> "1M";
        };
    }

    // 1M, 1Y는 모두 일봉 기준으로 조회한다.
    private String toKisPeriodCode(String period) {
        return "D";
    }

    private LocalDate calculateStartDate(LocalDate endDate, String period) {
        return switch (period.toUpperCase()) {
            case "1M" -> endDate.minusMonths(1);
            case "1Y" -> endDate.minusYears(1);
            default -> endDate.minusMonths(1);
        };
    }

    private String formatDateLabel(String value) {
        if (value == null || value.length() != 8) {
            return value;
        }

        return value.substring(0, 4) + "-" + value.substring(4, 6) + "-" + value.substring(6, 8);
    }

    private String formatMinuteLabel(String date, String time) {
        if (time == null || time.length() != 6) {
            return formatDateLabel(date);
        }

        return formatDateLabel(date) + " " + time.substring(0, 2) + ":" + time.substring(2, 4);
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }

        return Long.parseLong(value.replaceAll("[^0-9-]", ""));
    }
}