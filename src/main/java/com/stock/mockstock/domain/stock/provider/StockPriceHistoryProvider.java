package com.stock.mockstock.domain.stock.provider;

import com.stock.mockstock.domain.stock.dto.StockPriceHistoryResponse;

import java.util.List;

public interface StockPriceHistoryProvider {

    // 종목코드와 기간을 기준으로 차트 가격 이력을 조회한다.
    List<StockPriceHistoryResponse> getPriceHistories(String symbol, String period);
}