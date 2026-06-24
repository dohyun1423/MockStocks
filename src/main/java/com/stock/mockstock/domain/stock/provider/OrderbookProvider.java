package com.stock.mockstock.domain.stock.provider;

import com.stock.mockstock.domain.stock.dto.OrderbookResponse;

public interface OrderbookProvider {

    // 종목코드 기준으로 호가 데이터를 조회한다.
    OrderbookResponse getOrderbook(String symbol);
}