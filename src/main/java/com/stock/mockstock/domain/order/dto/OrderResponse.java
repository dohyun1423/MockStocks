// 매수와 매도 처리 결과 응답 DTO
package com.stock.mockstock.domain.order.dto;

import com.stock.mockstock.domain.order.enumtype.OrderType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderResponse {

    private String stockName;
    private OrderType orderType;
    private Integer quantity;
    private Long price;
    private Long totalAmount;
    private Long cashBalance;
}