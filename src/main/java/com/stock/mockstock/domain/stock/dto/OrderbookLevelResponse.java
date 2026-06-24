package com.stock.mockstock.domain.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderbookLevelResponse {

    private Integer level;
    private Long askPrice;
    private Long askQuantity;
    private Double askRate;
    private Long bidPrice;
    private Long bidQuantity;
    private Double bidRate;
}