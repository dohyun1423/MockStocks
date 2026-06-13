// 회원 계정 정보를 저장하는 엔티티
package com.stock.mockstock.domain.user.entity;

import com.stock.mockstock.domain.user.enumtype.Role;
import com.stock.mockstock.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false) // 초기 자금
    private Long cash;

    // 매수 시 사용자의 현금을 차감
    public void decreaseCash(Long amount) {
        if (cash < amount) {
            throw new IllegalArgumentException("보유 현금이 부족합니다.");
        }

        this.cash -= amount;
    }

    // 매도 시 사용자의 현금을 증가
    public void increaseCash(Long amount) {
        this.cash += amount;
    }
}

