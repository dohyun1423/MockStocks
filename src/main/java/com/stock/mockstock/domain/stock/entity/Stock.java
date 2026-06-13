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

    // 업종 정보는 마스터 파일에서 바로 알기 어려우므로 기본값으로 저장
    @Column(nullable = false)
    private String sector;

    @Column(nullable = false)
    private Long currentPrice;

    @Column(nullable = false)
    private Long changePrice;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal changeRate;

    @Column(nullable = false)
    private Long volume;

    @Column(nullable = false)
    private Long marketCap;

    @Column(nullable = false)
    private Long listedShares;

    @Column(precision = 8, scale = 2)
    private BigDecimal per;

    @Column(precision = 12, scale = 2)
    private BigDecimal eps;

    @Column(precision = 6, scale = 2)
    private BigDecimal dividendYield;

    // 종목 마스터 파일에서 읽은 기본 정보로 신규 종목 생성
    public static Stock createMasterStock(
            String symbol,
            String name,
            String market,
            Long basePrice,
            Long listedShares,
            Long marketCap
    ) {
        return Stock.builder()
                .symbol(symbol)
                .name(name)
                .market(market)
                .sector("미분류")
                .currentPrice(basePrice)
                .changePrice(0L)
                .changeRate(BigDecimal.ZERO)
                .volume(0L)
                .marketCap(marketCap)
                .listedShares(listedShares)
                .per(BigDecimal.ZERO)
                .eps(BigDecimal.ZERO)
                .dividendYield(BigDecimal.ZERO)
                .build();
    }

    // 이미 존재하는 종목을 마스터 파일 기준으로 갱신
    public void updateMasterInfo(
            String name,
            String market,
            Long basePrice,
            Long listedShares,
            Long marketCap
    ) {
        this.name = name;
        this.market = market;
        this.currentPrice = basePrice;
        this.listedShares = listedShares;
        this.marketCap = marketCap;
    }
}