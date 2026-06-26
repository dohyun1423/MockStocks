package com.stock.mockstock.domain.order.service;

import com.stock.mockstock.domain.order.entity.Trade;
import com.stock.mockstock.domain.order.enumtype.OrderType;
import com.stock.mockstock.domain.order.repository.TradeRepository;
import com.stock.mockstock.domain.portfolio.entity.Holding;
import com.stock.mockstock.domain.portfolio.repository.HoldingRepository;
import com.stock.mockstock.domain.stock.entity.Stock;
import com.stock.mockstock.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderExecutionService {

    private final HoldingRepository holdingRepository;
    private final TradeRepository tradeRepository;

    public Long execute(
            User user,
            Stock stock,
            OrderType orderType,
            Integer quantity,
            Long price
    ) {
        Long totalAmount = calculateTotalAmount(price, quantity);

        if (orderType == OrderType.BUY) {
            executeBuy(user, stock, quantity, price, totalAmount);
            return totalAmount;
        }

        executeSell(user, stock, quantity, price, totalAmount);
        return totalAmount;
    }

    public Long calculateTotalAmount(Long price, Integer quantity) {
        try {
            return Math.multiplyExact(price, quantity.longValue());
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("주문 금액이 너무 큽니다.");
        }
    }

    private void executeBuy(
            User user,
            Stock stock,
            Integer quantity,
            Long price,
            Long totalAmount
    ) {
        if (user.getCash() < totalAmount) {
            throw new IllegalArgumentException("보유 현금이 부족합니다.");
        }

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
    }

    private void executeSell(
            User user,
            Stock stock,
            Integer quantity,
            Long price,
            Long totalAmount
    ) {
        Holding holding = holdingRepository.findByUserAndStock(user, stock)
                .orElseThrow(() -> new IllegalArgumentException("보유 중인 종목이 아닙니다."));

        if (holding.getQuantity() < quantity) {
            throw new IllegalArgumentException("보유 수량이 부족합니다.");
        }

        holding.sell(quantity);

        if (holding.isEmpty()) {
            holdingRepository.delete(holding);
        }

        user.increaseCash(totalAmount);
        saveTrade(user, stock, OrderType.SELL, quantity, price, totalAmount);
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
}