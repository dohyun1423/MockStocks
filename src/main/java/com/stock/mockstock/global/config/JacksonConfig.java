// Jackson ObjectMapper를 스프링 빈으로 등록하는 설정 파일
package com.stock.mockstock.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    // JSON 변환에 사용할 ObjectMapper를 등록
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
