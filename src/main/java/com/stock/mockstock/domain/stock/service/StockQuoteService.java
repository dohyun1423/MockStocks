// KIS quote response를 우리 응답 객체로 변환하는 서비스
package com.stock.mockstock.domain.stock.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.mockstock.domain.stock.dto.StockQuoteResponse;
import com.stock.mockstock.domain.stock.dto.kis.KisQuoteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockQuoteService {

    private final ObjectMapper objectMapper;

    // 종목 코드로 현재가 정보를 조회
    public StockQuoteResponse getQuote(String symbol) {
        String dummyJson = createDummyKisQuoteJson(symbol);

        try {
            KisQuoteResponse kisQuoteResponse = objectMapper.readValue(dummyJson, KisQuoteResponse.class);
            return StockQuoteResponse.from(kisQuoteResponse);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("KIS 현재가 응답 파싱에 실패했습니다.", e);
        }
    }

    // 실제 KIS API 연결 전 사용할 더미 응답 생성
    private String createDummyKisQuoteJson(String symbol) {
        return """
                {
                  "rt_cd": "0",
                  "msg_cd": "MCA00000",
                  "msg1": "정상처리 되었습니다.",
                  "output": {
                    "stck_shrn_iscd": "%s",
                    "hts_kor_isnm": "%s",
                    "stck_prpr": "%s",
                    "prdy_vrss": "%s",
                    "prdy_vrss_sign": "2",
                    "prdy_ctrt": "%s",
                    "acml_vol": "%s",
                    "acml_tr_pbmn": "%s",
                    "stck_oprc": "%s",
                    "stck_hgpr": "%s",
                    "stck_lwpr": "%s",
                    "stck_sdpr": "%s"
                  }
                }
                """.formatted(
                symbol,
                getDummyName(symbol),
                getDummyCurrentPrice(symbol),
                "1200",
                "1.69",
                "15432000",
                "1111100000000",
                "71000",
                "72500",
                "70500",
                "70800"
        );
    }

    private String getDummyName(String symbol) {
        return switch (symbol) {
            case "005930" -> "삼성전자";
            case "000660" -> "SK하이닉스";
            case "035420" -> "NAVER";
            default -> "UNKNOWN";
        };
    }

    private String getDummyCurrentPrice(String symbol) {
        return switch (symbol) {
            case "005930" -> "72000";
            case "000660" -> "188500";
            case "035420" -> "219500";
            default -> "0";
        };
    }
}
