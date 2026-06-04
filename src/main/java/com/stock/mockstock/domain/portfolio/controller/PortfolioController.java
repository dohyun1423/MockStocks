// 내 모의투자 계좌와 보유 주식 API 요청을 받는 컨트롤러
package com.stock.mockstock.domain.portfolio.controller;

import com.stock.mockstock.domain.portfolio.dto.PortfolioResponse;
import com.stock.mockstock.domain.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    // 내 현금과 보유 주식 목록 조회
    @GetMapping
    public PortfolioResponse getMyPortfolio(Authentication authentication) {
        return portfolioService.getMyPortfolio(authentication.getName());
    }
}