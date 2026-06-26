package com.stock.mockstock.domain.order.dto;

import com.stock.mockstock.domain.order.enumtype.MarketSession;
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

    // EXECUTED 또는 RESERVED
    private String orderStatus;

    // 현재 거래 세션
    private MarketSession marketSession;

    // 예약주문이면 pendingOrderId가 내려간다.
    private Long pendingOrderId;

    private String message;
}