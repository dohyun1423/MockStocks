package com.stock.mockstock.domain.order.enumtype;

public enum MarketSession {
    PRE_MARKET,
    OPENING_AUCTION,
    REGULAR,
    CLOSING_AUCTION,
    AFTER_MARKET_WAIT,
    AFTER_MARKET_CLOSING_PRICE,
    AFTER_HOURS_SINGLE_PRICE,
    RESERVATION,
    CLOSED
}