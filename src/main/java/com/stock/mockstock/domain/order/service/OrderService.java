package com.stock.mockstock.domain.order.service;

import com.stock.mockstock.domain.order.dto.OrderRequest;
import com.stock.mockstock.domain.order.dto.OrderResponse;
import com.stock.mockstock.domain.order.entity.PendingOrder;
import com.stock.mockstock.domain.order.enumtype.MarketSession;
import com.stock.mockstock.domain.order.enumtype.OrderType;
import com.stock.mockstock.domain.order.enumtype.PendingOrderStatus;
import com.stock.mockstock.domain.order.repository.PendingOrderRepository;
import com.stock.mockstock.domain.stock.dto.StockQuoteResponse;
import com.stock.mockstock.domain.stock.entity.Stock;
import com.stock.mockstock.domain.stock.repository.StockRepository;
import com.stock.mockstock.domain.stock.service.StockQuoteService;
import com.stock.mockstock.domain.user.entity.User;
import com.stock.mockstock.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final PendingOrderRepository pendingOrderRepository;
    private final StockQuoteService stockQuoteService;
    private final MarketSessionService marketSessionService;
    private final OrderExecutionService orderExecutionService;

    public OrderResponse buy(String email, OrderRequest request) {
        return placeOrder(email, request, OrderType.BUY);
    }

    public OrderResponse sell(String email, OrderRequest request) {
        return placeOrder(email, request, OrderType.SELL);
    }

    private OrderResponse placeOrder(
            String email,
            OrderRequest request,
            OrderType orderType
    ) {
        validateOrderRequest(request);

        User user = getUser(email);
        Stock stock = getStockBySymbol(request.getSymbol());
        MarketSession session = marketSessionService.getCurrentSession();

        if (!marketSessionService.isOrderAvailable(session)) {
            throw new IllegalArgumentException("현재는 주문할 수 없는 시간입니다.");
        }

        Long currentPrice = getCurrentPrice(stock);
        Long orderPrice = resolveOrderPrice(request, currentPrice);
        Long totalAmount = orderExecutionService.calculateTotalAmount(orderPrice, request.getQuantity());

        if (marketSessionService.isImmediateExecution(session)) {
            orderExecutionService.execute(
                    user,
                    stock,
                    orderType,
                    request.getQuantity(),
                    orderPrice
            );

            return new OrderResponse(
                    stock.getName(),
                    orderType,
                    request.getQuantity(),
                    orderPrice,
                    totalAmount,
                    user.getCash(),
                    "EXECUTED",
                    session,
                    null,
                    "주문이 체결되었습니다."
            );
        }

        PendingOrder pendingOrder = savePendingOrder(
                user,
                stock,
                orderType,
                request.getQuantity(),
                orderPrice,
                session
        );

        return new OrderResponse(
                stock.getName(),
                orderType,
                request.getQuantity(),
                orderPrice,
                totalAmount,
                user.getCash(),
                "RESERVED",
                session,
                pendingOrder.getId(),
                "현재 거래 세션에서는 예약 주문으로 접수되었습니다."
        );
    }

    private PendingOrder savePendingOrder(
            User user,
            Stock stock,
            OrderType orderType,
            Integer quantity,
            Long limitPrice,
            MarketSession session
    ) {
        PendingOrder pendingOrder = PendingOrder.builder()
                .user(user)
                .stock(stock)
                .orderType(orderType)
                .quantity(quantity)
                .limitPrice(limitPrice)
                .marketSession(session)
                .status(PendingOrderStatus.PENDING)
                .build();

        return pendingOrderRepository.save(pendingOrder);
    }

    private Long resolveOrderPrice(OrderRequest request, Long currentPrice) {
        if (request.getLimitPrice() != null && request.getLimitPrice() > 0) {
            return request.getLimitPrice();
        }

        return currentPrice;
    }

    private void validateOrderRequest(OrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("주문 요청이 비어 있습니다.");
        }

        if (request.getSymbol() == null || request.getSymbol().isBlank()) {
            throw new IllegalArgumentException("종목코드는 필수입니다.");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("수량은 1주 이상이어야 합니다.");
        }

        if (request.getLimitPrice() != null && request.getLimitPrice() <= 0) {
            throw new IllegalArgumentException("주문 가격은 0보다 커야 합니다.");
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    private Stock getStockBySymbol(String symbol) {
        String normalizedSymbol = normalizeSymbol(symbol);

        return stockRepository.findBySymbol(normalizedSymbol)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 종목입니다."));
    }

    private String normalizeSymbol(String symbol) {
        return symbol.trim().replaceAll("\\s+", "").toUpperCase();
    }

    private Long getCurrentPrice(Stock stock) {
        StockQuoteResponse quote = stockQuoteService.getQuote(stock.getSymbol());

        if (quote == null || quote.getCurrentPrice() == null || quote.getCurrentPrice() <= 0) {
            throw new IllegalArgumentException("현재가 정보를 가져올 수 없습니다.");
        }

        return quote.getCurrentPrice();
    }
}