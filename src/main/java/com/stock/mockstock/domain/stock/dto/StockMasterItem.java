// MST 파일에서 파싱한 종목 마스터 한 건을 담는 DTO
package com.stock.mockstock.domain.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockMasterItem {

    private String symbol;
    private String name;
    private String market;
    private Long basePrice;
    private Long listedShares;
    private Long marketCap;
}