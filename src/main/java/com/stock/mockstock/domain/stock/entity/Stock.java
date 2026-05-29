// 주식 종목의 기본 정보와 지표를 저장하는 엔티티
package com.stock.mockstock.domain.stock.entity;

import com.stock.mockstock.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "stocks")
public class Stock extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 종목 코드 예: 005930
    @Column(nullable = false, unique = true)
    private String symbol;

    // 종목명 예: 삼성전자
    @Column(nullable = false)
    private String name;

    // 시장 예: KOSPI, KOSDAQ
    @Column(nullable = false)
    private String market;

    // 업종 예: 반도체, 금융, 바이오
    @Column(nullable = false)
    private String sector;

    // 현재가
    @Column(nullable = false)
    private Long currentPrice;

    // 전일 대비 가격
    @Column(nullable = false)
    private Long changePrice;

    // 등락률
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal changeRate;

    // 거래량
    @Column(nullable = false)
    private Long volume;

    // 시가총액
    @Column(nullable = false)
    private Long marketCap;

    // 상장주식수
    @Column(nullable = false)
    private Long listedShares;

    // PER
    @Column(precision = 8, scale = 2)
    private BigDecimal per;

    // EPS
    @Column(precision = 12, scale = 2)
    private BigDecimal eps;

    // 배당수익률
    @Column(precision = 6, scale = 2)
    private BigDecimal dividendYield;
}
