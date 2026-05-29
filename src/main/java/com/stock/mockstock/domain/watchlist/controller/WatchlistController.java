// 관심종목 API 요청을 받는 컨트롤러
package com.stock.mockstock.domain.watchlist.controller;

import com.stock.mockstock.domain.watchlist.dto.WatchlistCreateRequest;
import com.stock.mockstock.domain.watchlist.dto.WatchlistResponse;
import com.stock.mockstock.domain.watchlist.service.WatchlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/watchlists")
public class WatchlistController {

    private final WatchlistService watchlistService;

    // 관심종목 추가 요청 처리
    @PostMapping
    public void addWatchlist(
            Authentication authentication,
            @RequestBody @Valid WatchlistCreateRequest request
    ) {
        watchlistService.addWatchlist(authentication.getName(), request);
    }

    // 내 관심종목 목록 조회
    @GetMapping
    public List<WatchlistResponse> getMyWatchlists(Authentication authentication) {
        return watchlistService.getMyWatchlists(authentication.getName());
    }

    // 관심종목 삭제 요청 처리
    @DeleteMapping
    public void removeWatchlist(
            Authentication authentication,
            @RequestParam String stockName
    ) {
        watchlistService.removeWatchlist(authentication.getName(), stockName);
    }
}
