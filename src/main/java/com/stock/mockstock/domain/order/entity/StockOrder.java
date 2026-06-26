package com.stock.mockstock.domain.order.entity;

import com.stock.mockstock.domain.order.enumtype.MarketSession;
import com.stock.mockstock.domain.order.enumtype.OrderType;
import com.stock.mockstock.domain.order.enumtype.StockOrderStatus;
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
@Table(name = "stock_orders")
public class StockOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문을 넣은 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 주문 대상 종목
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // 매수 또는 매도
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    // 사용자가 지정한 주문 가격
    @Column(nullable = false)
    private Long orderPrice;

    // 최초 주문 수량
    @Column(nullable = false)
    private Integer quantity;

    // 지금까지 체결된 수량
    @Column(nullable = false)
    private Integer executedQuantity;

    // 아직 체결되지 않은 수량
    @Column(nullable = false)
    private Integer remainingQuantity;

    // 매수 주문에서 묶어둔 현금
    @Column(nullable = false)
    private Long reservedAmount;

    // 매도 주문에서 묶어둔 주식 수량
    @Column(nullable = false)
    private Integer reservedQuantity;

    // 주문 접수 당시의 거래 세션
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarketSession marketSession;

    // 주문 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockOrderStatus status;

    private LocalDateTime executedAt;

    private LocalDateTime canceledAt;

    @Column(length = 500)
    private String failureReason;

    // 주문이 아직 취소 가능한 상태인지 확인한다.
    public boolean isCancelable() {
        return status == StockOrderStatus.PENDING
                || status == StockOrderStatus.PARTIALLY_FILLED;
    }

    // 체결 수량을 반영하고 남은 수량에 따라 상태를 갱신한다.
    public void fill(Integer executionQuantity) {
        if (executionQuantity == null || executionQuantity <= 0) {
            throw new IllegalArgumentException("체결 수량은 1주 이상이어야 합니다.");
        }

        if (executionQuantity > remainingQuantity) {
            throw new IllegalArgumentException("체결 수량이 남은 주문 수량보다 많습니다.");
        }

        this.executedQuantity += executionQuantity;
        this.remainingQuantity -= executionQuantity;

        if (this.remainingQuantity == 0) {
            this.status = StockOrderStatus.FILLED;
            this.executedAt = LocalDateTime.now();
            return;
        }

        this.status = StockOrderStatus.PARTIALLY_FILLED;
    }

    // 미체결 주문을 취소 상태로 바꾼다.
    public void cancel() {
        if (!isCancelable()) {
            throw new IllegalArgumentException("취소할 수 없는 주문입니다.");
        }

        this.status = StockOrderStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }

    // 체결 처리 중 실패한 주문으로 표시한다.
    public void markFailed(String reason) {
        this.status = StockOrderStatus.FAILED;
        this.failureReason = reason;
    }
}
