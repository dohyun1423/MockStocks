// 종목 검색과 상세 조회 비즈니스 로직을 처리하는 서비스
package com.stock.mockstock.domain.stock.service;

import com.stock.mockstock.domain.stock.dto.StockDetailResponse;
import com.stock.mockstock.domain.stock.dto.StockSearchResponse;
import com.stock.mockstock.domain.stock.entity.Stock;
import com.stock.mockstock.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;

    // 키워드로 종목명 또는 종목코드 검색
    public List<StockSearchResponse> searchStocks(String keyword) {
        return stockRepository
                .findByNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(StockSearchResponse::from)
                .toList();
    }

    // 종목 id로 상세 정보 조회
    public StockDetailResponse getStockDetail(Long stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 종목입니다."));

        return StockDetailResponse.from(stock);
    }

    // 종목명으로 상세 정보 조회
    public StockDetailResponse getStockDetailByName(String name) {
        Stock stock = stockRepository
                .findFirstByNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(name, name)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 종목입니다."));

        return StockDetailResponse.from(stock);
    }

    // 종목 코드로 상세 정보 조회
    public StockDetailResponse getStockDetailBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 종목입니다."));

        return StockDetailResponse.from(stock);
    }
}
