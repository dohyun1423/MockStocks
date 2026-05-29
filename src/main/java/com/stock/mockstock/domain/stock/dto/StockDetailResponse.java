// 종목 상세 정보를 내려주는 응답 DTO
package com.stock.mockstock.domain.stock.dto;

import com.stock.mockstock.domain.stock.entity.Stock;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class StockDetailResponse {

    private Long id;
    private String symbol;
    private String name;
    private String market;
    private String sector;
    private Long currentPrice;
    private Long changePrice;
    private BigDecimal changeRate;
    private Long volume;
    private Long marketCap;
    private Long listedShares;
    private BigDecimal per;
    private BigDecimal eps;
    private BigDecimal dividendYield;

    public static StockDetailResponse from(Stock stock) {
        return new StockDetailResponse(
                stock.getId(),
                stock.getSymbol(),
                stock.getName(),
                stock.getMarket(),
                stock.getSector(),
                stock.getCurrentPrice(),
                stock.getChangePrice(),
                stock.getChangeRate(),
                stock.getVolume(),
                stock.getMarketCap(),
                stock.getListedShares(),
                stock.getPer(),
                stock.getEps(),
                stock.getDividendYield()
        );
    }
}
