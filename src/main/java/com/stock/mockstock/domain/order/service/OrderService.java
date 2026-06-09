// 현재가 기준 즉시 매수와 매도 처리를 담당하는 서비스
package com.stock.mockstock.domain.order.service;

import com.stock.mockstock.domain.order.dto.OrderRequest;
import com.stock.mockstock.domain.order.dto.OrderResponse;
import com.stock.mockstock.domain.order.entity.Trade;
import com.stock.mockstock.domain.order.enumtype.OrderType;
import com.stock.mockstock.domain.order.repository.TradeRepository;
import com.stock.mockstock.domain.portfolio.entity.Holding;
import com.stock.mockstock.domain.portfolio.repository.HoldingRepository;
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
    private final HoldingRepository holdingRepository;
    private final TradeRepository tradeRepository;
    private final StockQuoteService stockQuoteService;

    // 현재가 기준 즉시 매수 처리
    public OrderResponse buy(String email, OrderRequest request) {
        validateOrderRequest(request);

        User user = getUser(email);
        Stock stock = getStockBySymbol(request.getSymbol());
        int quantity = request.getQuantity();
        Long price = getCurrentPrice(stock);
        Long totalAmount = calculateTotalAmount(price, quantity);

        validateBuyAmount(user, totalAmount);
        user.decreaseCash(totalAmount);

        Holding holding = holdingRepository.findByUserAndStock(user, stock)
                .orElseGet(() -> Holding.builder()
                        .user(user)
                        .stock(stock)
                        .quantity(0)
                        .averagePrice(0L)
                        .build());

        holding.buy(quantity, price);
        holdingRepository.save(holding);

        saveTrade(user, stock, OrderType.BUY, quantity, price, totalAmount);

        return new OrderResponse(
                stock.getName(),
                OrderType.BUY,
                quantity,
                price,
                totalAmount,
                user.getCash()
        );
    }

    // 현재가 기준 즉시 매도 처리
    public OrderResponse sell(String email, OrderRequest request) {
        validateOrderRequest(request);

        User user = getUser(email);
        Stock stock = getStockBySymbol(request.getSymbol());
        int quantity = request.getQuantity();
        Long price = getCurrentPrice(stock);
        Long totalAmount = calculateTotalAmount(price, quantity);

        Holding holding = holdingRepository.findByUserAndStock(user, stock)
                .orElseThrow(() -> new IllegalArgumentException("보유 중인 종목이 아닙니다."));

        validateSellQuantity(holding, quantity);
        holding.sell(quantity);

        if (holding.isEmpty()) {
            holdingRepository.delete(holding);
        }

        user.increaseCash(totalAmount);
        saveTrade(user, stock, OrderType.SELL, quantity, price, totalAmount);

        return new OrderResponse(
                stock.getName(),
                OrderType.SELL,
                quantity,
                price,
                totalAmount,
                user.getCash()
        );
    }

    // 주문 요청의 필수값과 수량을 서비스 계층에서 한 번 더 검증
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
    }

    private void validateBuyAmount(User user, Long totalAmount) {
        if (user.getCash() < totalAmount) {
            throw new IllegalArgumentException("보유 현금이 부족합니다.");
        }
    }

    private void validateSellQuantity(Holding holding, Integer quantity) {
        if (holding.getQuantity() < quantity) {
            throw new IllegalArgumentException("보유 수량이 부족합니다.");
        }
    }

    private Long calculateTotalAmount(Long price, Integer quantity) {
        try {
            return Math.multiplyExact(price, quantity.longValue());
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("주문 금액이 너무 큽니다.");
        }
    }

    private void saveTrade(
            User user,
            Stock stock,
            OrderType orderType,
            Integer quantity,
            Long price,
            Long totalAmount
    ) {
        Trade trade = Trade.builder()
                .user(user)
                .stock(stock)
                .orderType(orderType)
                .quantity(quantity)
                .price(price)
                .totalAmount(totalAmount)
                .build();

        tradeRepository.save(trade);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    // 종목코드를 정규화해서 주문 대상 종목을 조회
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
