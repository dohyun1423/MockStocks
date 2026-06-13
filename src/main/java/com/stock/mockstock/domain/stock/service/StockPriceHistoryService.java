package com.stock.mockstock.domain.stock.service;

import com.stock.mockstock.domain.stock.dto.StockPriceHistoryResponse;
import com.stock.mockstock.domain.stock.provider.StockPriceHistoryProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockPriceHistoryService {

    private final StockPriceHistoryProvider stockPriceHistoryProvider;

    // Provider를 통해 더미 또는 KIS 차트 데이터를 조회한다.
    public List<StockPriceHistoryResponse> getPriceHistories(String symbol, String period) {
        return stockPriceHistoryProvider.getPriceHistories(symbol, period);
    }
}