package com.stock.mockstock.domain.watchlist.dto;

import com.stock.mockstock.domain.stock.entity.Stock;
import com.stock.mockstock.domain.watchlist.entity.Watchlist;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WatchlistResponse {

    private Long id;
    private String stockName;
    private String symbol;
    private String market;
    private Integer sortOrder;

    public static WatchlistResponse from(Watchlist watchlist, Stock stock) {
        return new WatchlistResponse(
                watchlist.getId(),
                watchlist.getStockName(),
                stock == null ? null : stock.getSymbol(),
                stock == null ? null : stock.getMarket(),
                watchlist.getSortOrder()
        );
    }
}