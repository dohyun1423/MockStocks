package com.stock.mockstock.domain.order.dto;

import com.stock.mockstock.domain.order.enumtype.MarketSession;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MarketSessionResponse {

    private MarketSession marketSession;
    private String displayName;
    private boolean orderAvailable;
    private boolean immediateExecution;
    private boolean reservationAvailable;
    private String message;
}