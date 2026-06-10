// KIS API를 호출해서 실제 현재가 정보를 가져오는 Provider
package com.stock.mockstock.domain.stock.provider;

import com.stock.mockstock.domain.stock.dto.StockQuoteResponse;
import com.stock.mockstock.domain.stock.dto.kis.KisQuoteResponse;
import com.stock.mockstock.domain.stock.kis.KisTokenService;
import com.stock.mockstock.global.config.KisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kis.provider", havingValue = "kis")
public class KisStockQuoteProvider implements StockQuoteProvider {

    private static final String INQUIRE_PRICE_PATH = "/uapi/domestic-stock/v1/quotations/inquire-price";
    private static final String DOMESTIC_STOCK_MARKET_CODE = "J";
    private static final String INQUIRE_PRICE_TR_ID = "FHKST01010100";

    private final KisProperties kisProperties;
    private final KisTokenService kisTokenService;
    private final RestClient.Builder restClientBuilder;

    private static final Logger log = LoggerFactory.getLogger(KisStockQuoteProvider.class);

    // KIS 현재가 API로 종목 현재가 조회
    @Override
    public StockQuoteResponse getQuote(String symbol) {
        String normalizedSymbol = normalizeSymbol(symbol);
        String accessToken = kisTokenService.getAccessToken();

        log.info("KIS quote provider used. symbol={}", normalizedSymbol);

        KisQuoteResponse response = restClientBuilder
                .baseUrl(kisProperties.getBaseUrl())
                .build()
                .get()
                .uri((uriBuilder) -> uriBuilder
                        .path(INQUIRE_PRICE_PATH)
                        .queryParam("FID_COND_MRKT_DIV_CODE", DOMESTIC_STOCK_MARKET_CODE)
                        .queryParam("FID_INPUT_ISCD", normalizedSymbol)
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header("appkey", kisProperties.getAppKey())
                .header("appsecret", kisProperties.getAppSecret())
                .header("tr_id", INQUIRE_PRICE_TR_ID)
                .retrieve()
                .body(KisQuoteResponse.class);

        validateResponse(response);

        return StockQuoteResponse.from(response);
    }

    private void validateResponse(KisQuoteResponse response) {
        if (response == null || response.getOutput() == null) {
            throw new IllegalStateException("KIS 현재가 응답이 비어 있습니다.");
        }

        if (!"0".equals(response.getRtCd())) {
            throw new IllegalStateException("KIS 현재가 조회에 실패했습니다. message=" + response.getMsg1());
        }
    }

    private String normalizeSymbol(String symbol) {
        return String.valueOf(symbol)
                .trim()
                .replaceAll("\\s+", "")
                .toUpperCase();
    }
}