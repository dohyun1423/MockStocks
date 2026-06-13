// KIS 종목 마스터 MST 파일을 읽어서 stocks 테이블에 저장하는 서비스
package com.stock.mockstock.domain.stock.service;

import com.stock.mockstock.domain.stock.dto.StockMasterImportResponse;
import com.stock.mockstock.domain.stock.dto.StockMasterItem;
import com.stock.mockstock.domain.stock.entity.Stock;
import com.stock.mockstock.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockMasterImportService {

    private static final Charset MST_CHARSET = Charset.forName("MS949");

    private static final String KOSPI_FILE = "classpath:stock-master/kospi_code.mst";
    private static final String KOSDAQ_FILE = "classpath:stock-master/kosdaq_code.mst";

    private final StockRepository stockRepository;
    private final ResourceLoader resourceLoader;

    // KOSPI, KOSDAQ 종목 마스터를 모두 import
    public StockMasterImportResponse importDomesticStocks() {
        List<StockMasterItem> items = new ArrayList<>();

        items.addAll(readMasterFile(KOSPI_FILE, "KOSPI", 228));
        items.addAll(readMasterFile(KOSDAQ_FILE, "KOSDAQ", 222));

        int createdCount = 0;
        int updatedCount = 0;

        for (StockMasterItem item : items) {
            Stock stock = stockRepository.findBySymbol(item.getSymbol())
                    .orElse(null);

            if (stock == null) {
                stockRepository.save(Stock.createMasterStock(
                        item.getSymbol(),
                        item.getName(),
                        item.getMarket(),
                        item.getBasePrice(),
                        item.getListedShares(),
                        item.getMarketCap()
                ));
                createdCount++;
                continue;
            }

            stock.updateMasterInfo(
                    item.getName(),
                    item.getMarket(),
                    item.getBasePrice(),
                    item.getListedShares(),
                    item.getMarketCap()
            );
            updatedCount++;
        }

        return new StockMasterImportResponse(items.size(), createdCount, updatedCount);
    }

    // MST 파일 한 개를 읽어서 종목 목록으로 변환
    private List<StockMasterItem> readMasterFile(String location, String market, int detailLength) {
        Resource resource = resourceLoader.getResource(location);

        if (!resource.exists()) {
            throw new IllegalArgumentException("종목 마스터 파일을 찾을 수 없습니다. location=" + location);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), MST_CHARSET)
        )) {
            List<StockMasterItem> items = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                StockMasterItem item = parseLine(line, market, detailLength);

                if (item != null) {
                    items.add(item);
                }
            }

            return items;
        } catch (IOException e) {
            throw new IllegalStateException("종목 마스터 파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

    // KIS MST 고정폭 한 줄을 종목 정보로 파싱
    private StockMasterItem parseLine(String line, String market, int detailLength) {
        if (line.length() <= detailLength) {
            return null;
        }

        String basicPart = line.substring(0, line.length() - detailLength);
        String detailPart = line.substring(line.length() - detailLength);

        String symbol = normalizeSymbol(slice(basicPart, 0, 9));
        String name = slice(basicPart, 21, basicPart.length()).trim();

        if (symbol.isBlank() || name.isBlank()) {
            return null;
        }

        List<String> detailFields = "KOSPI".equals(market)
                ? splitFixedWidth(detailPart, kospiFieldWidths())
                : splitFixedWidth(detailPart, kosdaqFieldWidths());

        Long basePrice = parseLong(getField(detailFields, "KOSPI".equals(market) ? 31 : 26));
        Long listedShares = parseLong(getField(detailFields, "KOSPI".equals(market) ? 50 : 45));
        Long marketCap = parseLong(getField(detailFields, "KOSPI".equals(market) ? 65 : 60));

        return new StockMasterItem(
                symbol,
                name,
                market,
                basePrice,
                listedShares,
                marketCap
        );
    }

    private List<String> splitFixedWidth(String value, int[] widths) {
        List<String> fields = new ArrayList<>();
        int start = 0;

        for (int width : widths) {
            int end = Math.min(start + width, value.length());
            fields.add(value.substring(start, end).trim());
            start += width;

            if (start >= value.length()) {
                break;
            }
        }

        return fields;
    }

    private String getField(List<String> fields, int index) {
        if (index < 0 || index >= fields.size()) {
            return "";
        }

        return fields.get(index);
    }

    private String slice(String value, int start, int end) {
        if (value.length() <= start) {
            return "";
        }

        return value.substring(start, Math.min(end, value.length()));
    }

    private String normalizeSymbol(String value) {
        String symbol = value.trim().toUpperCase();

        if (symbol.startsWith("A")) {
            symbol = symbol.substring(1);
        }

        return symbol;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }

        String onlyNumber = value.replaceAll("[^0-9-]", "");

        if (onlyNumber.isBlank()) {
            return 0L;
        }

        return Long.parseLong(onlyNumber);
    }

    // KIS 공식 kospi_code.mst 파이썬 샘플의 field_specs 기준
    private int[] kospiFieldWidths() {
        return new int[]{
                2, 1, 4, 4, 4, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 9, 5, 5, 1, 1, 1, 2, 1, 1,
                1, 2, 2, 2, 3, 1, 3, 12, 12, 8,
                15, 21, 2, 7, 1, 1, 1, 1, 1, 9,
                9, 9, 5, 9, 8, 9, 3, 1, 1, 1
        };
    }

    // KIS 공식 kosdaq_code.mst 파이썬 샘플의 field_specs 기준
    private int[] kosdaqFieldWidths() {
        return new int[]{
                2, 1, 4, 4, 4, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 9, 5, 5, 1,
                1, 1, 2, 1, 1, 1, 2, 2, 2, 3,
                1, 3, 12, 12, 8, 15, 21, 2, 7, 1,
                1, 1, 1, 9, 9, 9, 5, 9, 8, 9,
                3, 1, 1, 1
        };
    }
}