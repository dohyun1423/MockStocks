package com.stock.mockstock.domain.watchlist.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class WatchlistOrderUpdateRequest {

    private List<Long> watchlistIds;
}