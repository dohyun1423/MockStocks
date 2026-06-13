// 현재가 Provider를 통해 종목 현재가 정보를 조회하는 서비스
package com.stock.mockstock.domain.stock.service;

import com.stock.mockstock.domain.stock.dto.StockQuoteResponse;
import com.stock.mockstock.domain.stock.provider.StockQuoteProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockQuoteService {

    private final StockQuoteProvider stockQuoteProvider;

    // 종목코드로 현재가 정보 조회
    public StockQuoteResponse getQuote(String symbol) {
        return stockQuoteProvider.getQuote(symbol);
    }
}
