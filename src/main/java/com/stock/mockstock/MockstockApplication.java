// MockStock 스프링 부트 애플리케이션 시작점
package com.stock.mockstock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MockstockApplication {

    // 애플리케이션 실행
    public static void main(String[] args) {
        SpringApplication.run(MockstockApplication.class, args);
    }

}
