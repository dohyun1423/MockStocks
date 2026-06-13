// 현재가 데이터를 가져오는 Provider 공통 규격
package com.stock.mockstock.domain.stock.provider;

import com.stock.mockstock.domain.stock.dto.StockQuoteResponse;

public interface StockQuoteProvider {

    // 종목코드로 현재가 정보를 조회
    StockQuoteResponse getQuote(String symbol);
}
