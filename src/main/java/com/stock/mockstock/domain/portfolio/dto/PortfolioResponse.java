// 사용자의 모의투자 계좌와 보유 주식 목록 응답 DTO
package com.stock.mockstock.domain.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PortfolioResponse {

    private Long cashBalance;
    private List<HoldingResponse> holdings;
}