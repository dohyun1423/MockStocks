// 거래내역 API 요청을 받는 컨트롤러
package com.stock.mockstock.domain.order.controller;

import com.stock.mockstock.domain.order.dto.TradeResponse;
import com.stock.mockstock.domain.order.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    // 내 거래내역 조회
    @GetMapping
    public List<TradeResponse> getMyTrades(Authentication authentication) {
        return tradeService.getMyTrades(authentication.getName());
    }
}