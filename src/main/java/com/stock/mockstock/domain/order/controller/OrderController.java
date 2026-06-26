package com.stock.mockstock.domain.order.controller;

import com.stock.mockstock.domain.order.dto.MarketSessionResponse;
import com.stock.mockstock.domain.order.dto.OrderRequest;
import com.stock.mockstock.domain.order.dto.OrderResponse;
import com.stock.mockstock.domain.order.service.MarketSessionService;
import com.stock.mockstock.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final MarketSessionService marketSessionService;

    @GetMapping("/session")
    public MarketSessionResponse getCurrentMarketSession() {
        return marketSessionService.getCurrentSessionResponse();
    }

    @PostMapping("/buy")
    public OrderResponse buy(
            Authentication authentication,
            @RequestBody @Valid OrderRequest request
    ) {
        return orderService.buy(authentication.getName(), request);
    }

    @PostMapping("/sell")
    public OrderResponse sell(
            Authentication authentication,
            @RequestBody @Valid OrderRequest request
    ) {
        return orderService.sell(authentication.getName(), request);
    }
}