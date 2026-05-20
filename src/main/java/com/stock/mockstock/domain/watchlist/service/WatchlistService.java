package com.stock.mockstock.domain.watchlist.service;

import com.stock.mockstock.domain.user.entity.User;
import com.stock.mockstock.domain.user.repository.UserRepository;
import com.stock.mockstock.domain.watchlist.dto.WatchlistCreateRequest;
import com.stock.mockstock.domain.watchlist.dto.WatchlistResponse;
import com.stock.mockstock.domain.watchlist.entity.Watchlist;
import com.stock.mockstock.domain.watchlist.repository.WatchlistRepository;
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

    public void addWatchlist(String email, WatchlistCreateRequest request) {
        User user = getUser(email);
        String stockName = request.getStockName().trim();

        if (watchlistRepository.existsByUserAndStockName(user, stockName)) {
            return;
        }

        Watchlist watchlist = Watchlist.builder()
                .user(user)
                .stockName(stockName)
                .build();

        watchlistRepository.save(watchlist);
    }

    @Transactional(readOnly = true)
    public List<WatchlistResponse> getMyWatchlists(String email) {
        User user = getUser(email);

        return watchlistRepository.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(WatchlistResponse::from)
                .toList();
    }

    public void removeWatchlist(String email, String stockName) {
        User user = getUser(email);

        watchlistRepository.deleteByUserAndStockName(user, stockName);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}