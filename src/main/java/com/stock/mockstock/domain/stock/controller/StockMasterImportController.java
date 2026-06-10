// 종목 마스터 import API 요청을 받는 컨트롤러
package com.stock.mockstock.domain.stock.controller;

import com.stock.mockstock.domain.stock.dto.StockMasterImportResponse;
import com.stock.mockstock.domain.stock.service.StockMasterImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/stocks/master")
public class StockMasterImportController {

    private final StockMasterImportService stockMasterImportService;

    // resources/stock-master 아래 MST 파일을 읽어서 stocks 테이블에 저장
    @PostMapping("/import")
    public StockMasterImportResponse importDomesticStocks() {
        return stockMasterImportService.importDomesticStocks();
    }
}