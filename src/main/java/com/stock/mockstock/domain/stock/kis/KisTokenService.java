// KIS API access token을 발급하고 만료 전까지 재사용하는 서비스
package com.stock.mockstock.domain.stock.kis;

import com.stock.mockstock.global.config.KisProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KisTokenService {

    private static final long TOKEN_EXPIRE_MARGIN_SECONDS = 300;

    private final KisProperties kisProperties;
    private final RestClient.Builder restClientBuilder;

    private String accessToken;
    private LocalDateTime expiresAt;

    // 만료되지 않은 토큰이 있으면 재사용하고, 없으면 새로 발급
    public synchronized String getAccessToken() {
        if (isTokenUsable()) {
            return accessToken;
        }

        KisTokenResponse response = requestAccessToken();

        this.accessToken = response.getAccessToken();
        this.expiresAt = LocalDateTime.now()
                .plusSeconds(response.getExpiresIn() - TOKEN_EXPIRE_MARGIN_SECONDS);

        return accessToken;
    }

    // KIS OAuth API로 access token 발급 요청
    private KisTokenResponse requestAccessToken() {
        KisTokenRequest request = new KisTokenRequest(
                "client_credentials",
                kisProperties.getAppKey(),
                kisProperties.getAppSecret()
        );

        return restClientBuilder
                .baseUrl(kisProperties.getBaseUrl())
                .build()
                .post()
                .uri("/oauth2/tokenP")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .retrieve()
                .body(KisTokenResponse.class);
    }

    private boolean isTokenUsable() {
        return accessToken != null
                && expiresAt != null
                && LocalDateTime.now().isBefore(expiresAt);
    }

    private record KisTokenRequest(
            String grant_type,
            String appkey,
            String appsecret
    ) {
    }

    @Getter
    private static class KisTokenResponse {

        private String access_token;
        private String token_type;
        private Long expires_in;

        public String getAccessToken() {
            return access_token;
        }

        public Long getExpiresIn() {
            return expires_in == null ? 0L : expires_in;
        }
    }
}