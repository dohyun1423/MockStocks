// 차트용 가격 이력 데이터를 생성하고 조회하는 서비스
package com.stock.mockstock.domain.stock.service;

import com.stock.mockstock.domain.stock.dto.StockPriceHistoryResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockPriceHistoryService {

    // 현재는 실제 API 대신 종목코드와 기간에 맞는 더미 가격 이력 생성
    public List<StockPriceHistoryResponse> getPriceHistories(String symbol, String period) {
        String normalizedPeriod = normalizePeriod(period);
        int pointCount = getPointCount(normalizedPeriod);
        long basePrice = getBasePrice(symbol);

        List<StockPriceHistoryResponse> histories = new ArrayList<>();

        for (int i = 0; i < pointCount; i++) {
            long closePrice = createClosePrice(basePrice, i, pointCount);
            long openPrice = closePrice - Math.round(Math.sin(i * 0.35) * basePrice * 0.008);
            long highPrice = Math.max(openPrice, closePrice) + Math.round(basePrice * 0.01);
            long lowPrice = Math.min(openPrice, closePrice) - Math.round(basePrice * 0.01);
            long volume = 800_000L + (long) i * 35_000L;

            histories.add(new StockPriceHistoryResponse(
                    createLabel(normalizedPeriod, i, pointCount),
                    openPrice,
                    highPrice,
                    lowPrice,
                    closePrice,
                    volume
            ));
        }

        return histories;
    }

    // 지원하지 않는 기간 값은 기본값 1M으로 처리
    private String normalizePeriod(String period) {
        if (period == null || period.isBlank()) {
            return "1M";
        }

        return switch (period.toUpperCase()) {
            case "1D", "1W", "1M", "1Y" -> period.toUpperCase();
            default -> "1M";
        };
    }

    // 기간별 차트 포인트 개수 설정
    private int getPointCount(String period) {
        return switch (period) {
            case "1D" -> 24;
            case "1W" -> 7;
            case "1Y" -> 12;
            default -> 30;
        };
    }

    // 종목별 기준 가격 설정
    private long getBasePrice(String symbol) {
        return switch (symbol) {
            case "005930" -> 72_000L;
            case "000660" -> 188_500L;
            case "035420" -> 219_500L;
            default -> 50_000L;
        };
    }

    // 차트가 너무 일직선으로 보이지 않도록 더미 종가 생성
    private long createClosePrice(long basePrice, int index, int pointCount) {
        long trend = (long) (index - pointCount / 2.0) * Math.max(10L, basePrice / 400L);
        long wave = Math.round(Math.sin(index * 0.7) * basePrice * 0.025);

        return basePrice + trend + wave;
    }

    // 기간별 x축 라벨 생성
    private String createLabel(String period, int index, int pointCount) {
        return switch (period) {
            case "1D" -> LocalTime.of(9, 0).plusMinutes(index * 15L).toString();
            case "1Y" -> LocalDate.now().minusMonths(pointCount - index - 1L).toString().substring(0, 7);
            default -> LocalDate.now().minusDays(pointCount - index - 1L).toString();
        };
    }
}