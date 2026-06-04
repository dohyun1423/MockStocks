// 매수와 매도 요청 DTO
package com.stock.mockstock.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class OrderRequest {

    @NotBlank(message = "종목코드는 필수입니다.")
    private String symbol;

    @Positive(message = "수량은 1주 이상이어야 합니다.")
    private Integer quantity;
}