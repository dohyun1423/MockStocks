// 관심종목 추가, 조회, 삭제 비즈니스 로직을 처리하는 서비스
package com.stock.mockstock.domain.watchlist.service;

import com.stock.mockstock.domain.user.entity.User;
import com.stock.mockstock.domain.user.repository.UserRepository;
import com.stock.mockstock.domain.watchlist.dto.WatchlistCreateRequest;
import com.stock.mockstock.domain.watchlist.dto.WatchlistResponse;
import com.stock.mockstock.domain.watchlist.entity.Watchlist;
import com.stock.mockstock.domain.watchlist.repository.WatchlistRepository;
import com.stock.mockstock.domain.stock.entity.Stock;
import com.stock.mockstock.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    // 로그인한 사용자의 관심종목 추가
    public void addWatchlist(String email, WatchlistCreateRequest request) {
        User user = getUser(email);
        String stockName = request.getStockName();

        if (watchlistRepository.existsByUserAndStockName(user, stockName)) {
            return;
        }

        Watchlist watchlist = Watchlist.builder()
                .user(user)
                .stockName(stockName)
                .build();

        watchlistRepository.save(watchlist);
    }

    // 로그인한 사용자의 관심종목 목록 조회
    @Transactional(readOnly = true)
    public List<WatchlistResponse> getMyWatchlists(String email) {
        User user = getUser(email);

        return watchlistRepository.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toWatchlistResponse)
                .toList();
    }

    // 관심종목명으로 stock 테이블을 조회해서 symbol 정보를 함께 내려줌
    private WatchlistResponse toWatchlistResponse(Watchlist watchlist) {
        String stockName = watchlist.getStockName();

        Stock stock = stockRepository.findFirstByNameIgnoreCase(stockName)
                .or(() -> stockRepository.findFirstByNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(stockName, stockName))
                .orElse(null);

        return WatchlistResponse.from(watchlist, stock);
    }

    // 로그인한 사용자의 관심종목 삭제
    public void removeWatchlist(String email, String stockName) {
        User user = getUser(email);

        watchlistRepository.deleteByUserAndStockName(user, stockName);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}