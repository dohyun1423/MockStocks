// 거래내역 API 요청을 받는 컨트롤러
package com.stock.mockstock.domain.order.controller;

import com.stock.mockstock.domain.order.dto.TradeResponse;
import com.stock.mockstock.domain.order.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    // 내 거래내역 조회, symbol이 있으면 해당 종목만 조회
    @GetMapping
    public List<TradeResponse> getMyTrades(
            Authentication authentication,
            @RequestParam(required = false) String symbol
    ) {
        return tradeService.getMyTrades(authentication.getName(), symbol);
    }
}
