// 사용자가 현재 보유 중인 종목 정보를 저장하는 엔티티
package com.stock.mockstock.domain.portfolio.entity;

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
@Table(
        name = "holdings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_holding_user_stock",
                        columnNames = {"user_id", "stock_id"}
                )
        }
)
public class Holding extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 보유 주식의 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 보유 중인 종목
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // 보유 수량
    @Column(nullable = false)
    private Integer quantity;

    // 평균 매수가
    @Column(nullable = false)
    private Long averagePrice;

    // 추가 매수 시 보유 수량과 평균단가 갱신
    public void buy(Integer buyQuantity, Long buyPrice) {
        long currentTotalAmount = averagePrice * quantity;
        long buyTotalAmount = buyPrice * buyQuantity;

        this.quantity += buyQuantity;
        this.averagePrice = (currentTotalAmount + buyTotalAmount) / this.quantity;
    }

    // 매도 시 보유 수량 차감
    public void sell(Integer sellQuantity) {
        if (quantity < sellQuantity) {
            throw new IllegalArgumentException("보유 수량이 부족합니다.");
        }

        this.quantity -= sellQuantity;
    }

    // 보유 수량이 0인지 확인
    public boolean isEmpty() {
        return quantity == 0;
    }
}