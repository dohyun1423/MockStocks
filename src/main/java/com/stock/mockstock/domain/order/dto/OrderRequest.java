package com.stock.mockstock.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class OrderRequest {

    @NotBlank(message = "종목코드는 필수입니다.")
    private String symbol;

    @NotNull(message = "수량은 필수입니다.")
    @Positive(message = "수량은 1주 이상이어야 합니다.")
    private Integer quantity;

    // 예약 주문 또는 시간외 주문에서 사용할 기준 가격
    private Long limitPrice;
}