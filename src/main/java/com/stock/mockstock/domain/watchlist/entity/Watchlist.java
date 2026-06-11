// 사용자별 관심종목을 저장하는 엔티티
package com.stock.mockstock.domain.watchlist.entity;

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
        name = "watchlists",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_watchlist_user_stock",
                        columnNames = {"user_id", "stock_name"}
                )
        }
)
public class Watchlist extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 관심종목을 등록한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 관심종목 이름
    @Column(name = "stock_name", nullable = false)
    private String stockName;
}