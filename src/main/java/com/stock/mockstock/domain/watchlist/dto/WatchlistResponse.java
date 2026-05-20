package com.stock.mockstock.domain.watchlist.dto;

import com.stock.mockstock.domain.watchlist.entity.Watchlist;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WatchlistResponse {

    private Long id;
    private String stockName;

    public static WatchlistResponse from(Watchlist watchlist) {
        return new WatchlistResponse(
                watchlist.getId(),
                watchlist.getStockName()
        );
    }
}