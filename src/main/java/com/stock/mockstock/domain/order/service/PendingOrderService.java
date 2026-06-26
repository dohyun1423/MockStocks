package com.stock.mockstock.domain.order.service;

import com.stock.mockstock.domain.order.entity.PendingOrder;
import com.stock.mockstock.domain.order.enumtype.MarketSession;
import com.stock.mockstock.domain.order.enumtype.PendingOrderStatus;
import com.stock.mockstock.domain.order.repository.PendingOrderRepository;
import com.stock.mockstock.domain.stock.dto.StockQuoteResponse;
import com.stock.mockstock.domain.stock.service.StockQuoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PendingOrderService {

    private final PendingOrderRepository pendingOrderRepository;
    private final StockQuoteService stockQuoteService;
    private final MarketSessionService marketSessionService;
    private final OrderExecutionService orderExecutionService;

    // 예약 주문은 1분마다 현재 세션과 현재가를 기준으로 체결 가능 여부를 확인한다.
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void executePendingOrders() {
        MarketSession session = marketSessionService.getCurrentSession();

        if (!marketSessionService.isImmediateExecution(session)) {
            return;
        }

        List<PendingOrder> pendingOrders = pendingOrderRepository
                .findAllByStatusOrderByCreatedAtAsc(PendingOrderStatus.PENDING);

        for (PendingOrder pendingOrder : pendingOrders) {
            executeIfPossible(pendingOrder);
        }
    }

    private void executeIfPossible(PendingOrder pendingOrder) {
        try {
            Long currentPrice = getCurrentPrice(pendingOrder);

            if (!pendingOrder.isExecutableByPrice(currentPrice)) {
                return;
            }

            orderExecutionService.execute(
                    pendingOrder.getUser(),
                    pendingOrder.getStock(),
                    pendingOrder.getOrderType(),
                    pendingOrder.getQuantity(),
                    currentPrice
            );

            pendingOrder.markExecuted();

            log.info(
                    "Pending order executed. pendingOrderId={}, symbol={}, orderType={}, quantity={}, price={}",
                    pendingOrder.getId(),
                    pendingOrder.getStock().getSymbol(),
                    pendingOrder.getOrderType(),
                    pendingOrder.getQuantity(),
                    currentPrice
            );
        } catch (RuntimeException e) {
            pendingOrder.markFailed(e.getMessage());

            log.warn(
                    "Pending order failed. pendingOrderId={}, reason={}",
                    pendingOrder.getId(),
                    e.getMessage()
            );
        }
    }

    private Long getCurrentPrice(PendingOrder pendingOrder) {
        StockQuoteResponse quote = stockQuoteService.getQuote(pendingOrder.getStock().getSymbol());

        if (quote == null || quote.getCurrentPrice() == null || quote.getCurrentPrice() <= 0) {
            throw new IllegalArgumentException("현재가 정보를 가져올 수 없습니다.");
        }

        return quote.getCurrentPrice();
    }
}