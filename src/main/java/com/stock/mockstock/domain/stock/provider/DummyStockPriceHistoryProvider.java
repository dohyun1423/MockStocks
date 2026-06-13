package com.stock.mockstock.domain.stock.provider;

import com.stock.mockstock.domain.stock.dto.StockPriceHistoryResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(
        name = "kis.provider",
        havingValue = "dummy",
        matchIfMissing = true
)
public class DummyStockPriceHistoryProvider implements StockPriceHistoryProvider {

    // KIS 차트 API 연결 전까지 사용할 더미 가격 이력을 생성한다.
    @Override
    public List<StockPriceHistoryResponse> getPriceHistories(String symbol, String period) {
        String normalizedSymbol = normalizeSymbol(symbol);
        String normalizedPeriod = normalizePeriod(period);
        int pointCount = getPointCount(normalizedPeriod);
        long basePrice = getBasePrice(normalizedSymbol);

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

    private int getPointCount(String period) {
        return switch (period) {
            case "1D" -> 24;
            case "1W" -> 7;
            case "1Y" -> 12;
            default -> 30;
        };
    }

    private long getBasePrice(String symbol) {
        return switch (symbol) {
            case "005930" -> 72_000L;
            case "000660" -> 188_500L;
            case "035420" -> 219_500L;
            default -> 50_000L;
        };
    }

    private long createClosePrice(long basePrice, int index, int pointCount) {
        long trend = (long) (index - pointCount / 2.0) * Math.max(10L, basePrice / 400L);
        long wave = Math.round(Math.sin(index * 0.7) * basePrice * 0.025);

        return basePrice + trend + wave;
    }

    private String createLabel(String period, int index, int pointCount) {
        return switch (period) {
            case "1D" -> LocalTime.of(9, 0).plusMinutes(index * 15L).toString();
            case "1Y" -> LocalDate.now().minusMonths(pointCount - index - 1L).toString().substring(0, 7);
            default -> LocalDate.now().minusDays(pointCount - index - 1L).toString();
        };
    }
}