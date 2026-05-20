package com.stock.mockstock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MockstockApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockstockApplication.class, args);
    }

}
