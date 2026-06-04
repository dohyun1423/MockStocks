// 사용자의 매수/매도 거래내역을 내려주는 응답 DTO
package com.stock.mockstock.domain.order.dto;

import com.stock.mockstock.domain.order.entity.Trade;
import com.stock.mockstock.domain.order.enumtype.OrderType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TradeResponse {

    private String stockName;
    private String symbol;
    private OrderType orderType;
    private Integer quantity;
    private Long price;
    private Long totalAmount;
    private LocalDateTime tradedAt;

    public static TradeResponse from(Trade trade) {
        return new TradeResponse(
                trade.getStock().getName(),
                trade.getStock().getSymbol(),
                trade.getOrderType(),
                trade.getQuantity(),
                trade.getPrice(),
                trade.getTotalAmount(),
                trade.getCreatedAt()
        );
    }
}