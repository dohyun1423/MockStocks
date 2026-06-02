// 차트에 사용할 종목 가격 이력 응답 DTO
package com.stock.mockstock.domain.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockPriceHistoryResponse {

    private String label;
    private Long openPrice;
    private Long highPrice;
    private Long lowPrice;
    private Long closePrice;
    private Long volume;
}