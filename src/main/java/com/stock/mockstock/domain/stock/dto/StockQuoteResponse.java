// 프론트에서 사용하기 좋게 현재가 정보를 정리한 응답 DTO
package com.stock.mockstock.domain.stock.dto;

import com.stock.mockstock.domain.stock.dto.kis.KisQuoteResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class StockQuoteResponse {

    private String symbol;
    private String name;
    private Long currentPrice;
    private Long changePrice;
    private BigDecimal changeRate;
    private Long volume;
    private Long tradingValue;
    private Long openPrice;
    private Long highPrice;
    private Long lowPrice;
    private Long basePrice;

    // KIS 원본 응답을 화면용 현재가 응답으로 변환
    public static StockQuoteResponse from(KisQuoteResponse kisQuoteResponse) {
        KisQuoteResponse.Output output = kisQuoteResponse.getOutput();

        return new StockQuoteResponse(
                output.getStckShrnIscd(),
                output.getHtsKorIsnm(),
                parseLong(output.getStckPrpr()),
                parseLong(output.getPrdyVrss()),
                parseBigDecimal(output.getPrdyCtrt()),
                parseLong(output.getAcmlVol()),
                parseLong(output.getAcmlTrPbmn()),
                parseLong(output.getStckOprc()),
                parseLong(output.getStckHgpr()),
                parseLong(output.getStckLwpr()),
                parseLong(output.getStckSdpr())
        );
    }

    private static Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Long.parseLong(value);
    }

    private static BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return new BigDecimal(value);
    }
}
