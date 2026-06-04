// 매수와 매도 API 요청을 받는 컨트롤러
package com.stock.mockstock.domain.order.controller;

import com.stock.mockstock.domain.order.dto.OrderRequest;
import com.stock.mockstock.domain.order.dto.OrderResponse;
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

    // 현재가 기준 즉시 매수
    @PostMapping("/buy")
    public OrderResponse buy(
            Authentication authentication,
            @RequestBody @Valid OrderRequest request
    ) {
        return orderService.buy(authentication.getName(), request);
    }

    // 현재가 기준 즉시 매도
    @PostMapping("/sell")
    public OrderResponse sell(
            Authentication authentication,
            @RequestBody @Valid OrderRequest request
    ) {
        return orderService.sell(authentication.getName(), request);
    }
}