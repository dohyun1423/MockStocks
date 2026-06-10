// KIS API 연결 전까지 사용할 더미 현재가 Provider
package com.stock.mockstock.domain.stock.provider;

import com.stock.mockstock.domain.stock.dto.StockQuoteResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(
        name = "kis.provider",
        havingValue = "dummy",
        matchIfMissing = true
)
public class DummyStockQuoteProvider implements StockQuoteProvider {

    // 종목코드별 더미 현재가 정보를 반환
    @Override
    public StockQuoteResponse getQuote(String symbol) {
        String normalizedSymbol = normalizeSymbol(symbol);

        return new StockQuoteResponse(
                normalizedSymbol,
                getDummyName(normalizedSymbol),
                getCurrentPrice(normalizedSymbol),
                getChangePrice(normalizedSymbol),
                getChangeRate(normalizedSymbol),
                getVolume(normalizedSymbol),
                getTradingValue(normalizedSymbol),
                getOpenPrice(normalizedSymbol),
                getHighPrice(normalizedSymbol),
                getLowPrice(normalizedSymbol),
                getBasePrice(normalizedSymbol)
        );
    }

    private String normalizeSymbol(String symbol) {
        return String.valueOf(symbol)
                .trim()
                .replaceAll("\\s+", "")
                .toUpperCase();
    }

    private String getDummyName(String symbol) {
        return switch (symbol) {
            case "005930" -> "삼성전자";
            case "000660" -> "SK하이닉스";
            case "035420" -> "NAVER";
            default -> "UNKNOWN";
        };
    }

    private Long getCurrentPrice(String symbol) {
        return switch (symbol) {
            case "005930" -> 72000L;
            case "000660" -> 188500L;
            case "035420" -> 219500L;
            default -> 0L;
        };
    }

    private Long getChangePrice(String symbol) {
        return switch (symbol) {
            case "000660" -> 1200L;
            case "035420" -> -1500L;
            default -> 800L;
        };
    }

    private BigDecimal getChangeRate(String symbol) {
        return switch (symbol) {
            case "000660" -> new BigDecimal("1.69");
            case "035420" -> new BigDecimal("-0.68");
            default -> new BigDecimal("1.12");
        };
    }

    private Long getVolume(String symbol) {
        return switch (symbol) {
            case "000660" -> 15432000L;
            case "035420" -> 3214500L;
            default -> 18765000L;
        };
    }

    private Long getTradingValue(String symbol) {
        return switch (symbol) {
            case "000660" -> 1111100000000L;
            case "035420" -> 705230000000L;
            default -> 1350800000000L;
        };
    }

    private Long getOpenPrice(String symbol) {
        return switch (symbol) {
            case "000660" -> 186000L;
            case "035420" -> 221000L;
            default -> 71400L;
        };
    }

    private Long getHighPrice(String symbol) {
        return switch (symbol) {
            case "000660" -> 190500L;
            case "035420" -> 222000L;
            default -> 72500L;
        };
    }

    private Long getLowPrice(String symbol) {
        return switch (symbol) {
            case "000660" -> 184500L;
            case "035420" -> 218000L;
            default -> 70500L;
        };
    }

    private Long getBasePrice(String symbol) {
        return switch (symbol) {
            case "000660" -> 187300L;
            case "035420" -> 221000L;
            default -> 71200L;
        };
    }
}
