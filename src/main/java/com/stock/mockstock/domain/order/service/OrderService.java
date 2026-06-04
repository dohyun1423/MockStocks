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
        User user = getUser(email);
        Stock stock = getStockBySymbol(request.getSymbol());
        Long price = getCurrentPrice(stock);
        Long totalAmount = price * request.getQuantity();

        user.decreaseCash(totalAmount);

        Holding holding = holdingRepository.findByUserAndStock(user, stock)
                .orElseGet(() -> Holding.builder()
                        .user(user)
                        .stock(stock)
                        .quantity(0)
                        .averagePrice(0L)
                        .build());

        holding.buy(request.getQuantity(), price);
        holdingRepository.save(holding);

        saveTrade(user, stock, OrderType.BUY, request.getQuantity(), price, totalAmount);

        return new OrderResponse(
                stock.getName(),
                OrderType.BUY,
                request.getQuantity(),
                price,
                totalAmount,
                user.getCash()
        );
    }

    // 현재가 기준 즉시 매도 처리
    public OrderResponse sell(String email, OrderRequest request) {
        User user = getUser(email);
        Stock stock = getStockBySymbol(request.getSymbol());
        Long price = getCurrentPrice(stock);
        Long totalAmount = price * request.getQuantity();

        Holding holding = holdingRepository.findByUserAndStock(user, stock)
                .orElseThrow(() -> new IllegalArgumentException("보유 중인 종목이 아닙니다."));

        holding.sell(request.getQuantity());

        if (holding.isEmpty()) {
            holdingRepository.delete(holding);
        }

        user.increaseCash(totalAmount);

        saveTrade(user, stock, OrderType.SELL, request.getQuantity(), price, totalAmount);

        return new OrderResponse(
                stock.getName(),
                OrderType.SELL,
                request.getQuantity(),
                price,
                totalAmount,
                user.getCash()
        );
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

    // 종목코드로 주문 대상 종목 조회
    private Stock getStockBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 종목입니다."));
    }

    private Long getCurrentPrice(Stock stock) {
        StockQuoteResponse quote = stockQuoteService.getQuote(stock.getSymbol());

        if (quote.getCurrentPrice() == null || quote.getCurrentPrice() <= 0) {
            throw new IllegalArgumentException("현재가 정보를 가져올 수 없습니다.");
        }

        return quote.getCurrentPrice();
    }
}