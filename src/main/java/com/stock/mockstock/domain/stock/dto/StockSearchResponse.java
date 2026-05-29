// 종목 검색 결과를 내려주는 응답 DTO
package com.stock.mockstock.domain.stock.dto;

import com.stock.mockstock.domain.stock.entity.Stock;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockSearchResponse {

    private Long id;
    private String symbol;
    private String name;
    private String market;

    public static StockSearchResponse from(Stock stock) {
        return new StockSearchResponse(
                stock.getId(),
                stock.getSymbol(),
                stock.getName(),
                stock.getMarket()
        );
    }
}
