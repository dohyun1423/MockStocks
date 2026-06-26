package com.stock.mockstock.domain.order.entity;

import com.stock.mockstock.domain.order.enumtype.MarketSession;
import com.stock.mockstock.domain.order.enumtype.OrderType;
import com.stock.mockstock.domain.order.enumtype.PendingOrderStatus;
import com.stock.mockstock.domain.stock.entity.Stock;
import com.stock.mockstock.domain.user.entity.User;
import com.stock.mockstock.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "pending_orders")
public class PendingOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예약 주문을 등록한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 예약 주문 대상 종목
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // 매수 또는 매도
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    // 주문 수량
    @Column(nullable = false)
    private Integer quantity;

    // 예약 주문 기준 가격
    @Column(nullable = false)
    private Long limitPrice;

    // 주문이 접수된 당시의 거래 세션
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarketSession marketSession;

    // 예약 주문 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PendingOrderStatus status;

    private LocalDateTime executedAt;

    @Column(length = 500)
    private String failureReason;

    public void markExecuted() {
        this.status = PendingOrderStatus.EXECUTED;
        this.executedAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        this.status = PendingOrderStatus.FAILED;
        this.failureReason = reason;
    }

    public boolean isExecutableByPrice(Long currentPrice) {
        if (currentPrice == null || currentPrice <= 0) {
            return false;
        }

        if (orderType == OrderType.BUY) {
            return currentPrice <= limitPrice;
        }

        return currentPrice >= limitPrice;
    }
}