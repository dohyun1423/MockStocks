// 매수와 매도 체결 내역을 저장하는 엔티티
package com.stock.mockstock.domain.order.entity;

import com.stock.mockstock.domain.order.enumtype.OrderType;
import com.stock.mockstock.domain.stock.entity.Stock;
import com.stock.mockstock.domain.user.entity.User;
import com.stock.mockstock.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "trades")
public class Trade extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 거래한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 거래한 종목
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // 매수 또는 매도
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    // 거래 수량
    @Column(nullable = false)
    private Integer quantity;

    // 체결 가격
    @Column(nullable = false)
    private Long price;

    // 총 거래 금액
    @Column(nullable = false)
    private Long totalAmount;
}