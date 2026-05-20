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

    @PostMapping
    public void addWatchlist(
            Authentication authentication,
            @RequestBody @Valid WatchlistCreateRequest request
    ) {
        watchlistService.addWatchlist(authentication.getName(), request);
    }

    @GetMapping
    public List<WatchlistResponse> getMyWatchlists(Authentication authentication) {
        return watchlistService.getMyWatchlists(authentication.getName());
    }

    @DeleteMapping
    public void removeWatchlist(
            Authentication authentication,
            @RequestParam String stockName
    ) {
        watchlistService.removeWatchlist(authentication.getName(), stockName);
    }
}