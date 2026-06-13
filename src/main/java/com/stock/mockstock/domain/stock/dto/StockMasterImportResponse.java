// 종목 마스터 파일 import 결과를 내려주는 응답 DTO
package com.stock.mockstock.domain.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockMasterImportResponse {

    private int totalCount;
    private int createdCount;
    private int updatedCount;
}