package com.stock.mockstock.domain.stock.provider;

import com.stock.mockstock.domain.stock.dto.StockPriceHistoryResponse;
import com.stock.mockstock.domain.stock.dto.kis.KisDailyChartResponse;
import com.stock.mockstock.domain.stock.kis.KisTokenService;
import com.stock.mockstock.global.config.KisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kis.provider", havingValue = "kis")
public class KisStockPriceHistoryProvider implements StockPriceHistoryProvider {

    private static final String DAILY_CHART_PATH = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";
    private static final String DOMESTIC_STOCK_MARKET_CODE = "J";
    private static final String DAILY_CHART_TR_ID = "FHKST03010100";
    private static final String ORG_ADJUST_PRICE = "0";
    private static final DateTimeFormatter KIS_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final KisProperties kisProperties;
    private final KisTokenService kisTokenService;
    private final RestClient.Builder restClientBuilder;

    // KIS 기간별 시세 API로 종목 차트 데이터를 조회한다.
    @Override
    public List<StockPriceHistoryResponse> getPriceHistories(String symbol, String period) {
        String normalizedSymbol = normalizeSymbol(symbol);
        String normalizedPeriod = normalizePeriod(period);
        String periodCode = toKisPeriodCode(normalizedPeriod);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(endDate, normalizedPeriod);

        log.info(
                "KIS chart provider used. symbol={}, uiPeriod={}, kisPeriod={}, startDate={}, endDate={}",
                normalizedSymbol,
                normalizedPeriod,
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
                        .queryParam("FID_INPUT_ISCD", normalizedSymbol)
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
                .retrieve()
                .body(KisDailyChartResponse.class);

        validateResponse(response);

        return response.getOutput2()
                .stream()
                .map(this::toResponse)
                .sorted(Comparator.comparing(StockPriceHistoryResponse::getLabel))
                .toList();
    }

    private StockPriceHistoryResponse toResponse(KisDailyChartResponse.Output output) {
        return new StockPriceHistoryResponse(
                formatLabel(output.getBusinessDate()),
                parseLong(output.getOpenPrice()),
                parseLong(output.getHighPrice()),
                parseLong(output.getLowPrice()),
                parseLong(output.getClosePrice()),
                parseLong(output.getAccumulatedVolume())
        );
    }

    private void validateResponse(KisDailyChartResponse response) {
        if (response == null || response.getOutput2() == null) {
            throw new IllegalStateException("KIS chart response is empty.");
        }

        if (!"0".equals(response.getRtCd())) {
            throw new IllegalStateException("KIS chart request failed. message=" + response.getMsg1());
        }
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

    private String toKisPeriodCode(String period) {
        // WebSocket/minute chart is not connected yet, so 1D/1W/1M use daily candles for now.
        return switch (period) {
            case "1Y" -> "M";
            default -> "D";
        };
    }

    private LocalDate calculateStartDate(LocalDate endDate, String period) {
        // Keep UI period and KIS request range aligned instead of loading oversized ranges.
        return switch (period.toUpperCase()) {
            case "1D" -> endDate.minusDays(1);
            case "1W" -> endDate.minusWeeks(1);
            case "1M" -> endDate.minusMonths(1);
            case "1Y" -> endDate.minusYears(1);
            default -> endDate.minusMonths(1);
        };
    }

    private String formatLabel(String value) {
        if (value == null || value.length() != 8) {
            return value;
        }

        return value.substring(0, 4) + "-" + value.substring(4, 6) + "-" + value.substring(6, 8);
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }

        return Long.parseLong(value);
    }
}
