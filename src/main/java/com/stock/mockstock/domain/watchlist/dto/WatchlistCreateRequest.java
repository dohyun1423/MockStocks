// 관심종목 추가 요청 값을 받는 DTO
package com.stock.mockstock.domain.watchlist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WatchlistCreateRequest {

    @NotBlank(message = "종목명은 필수입니다.")
    private String stockName;
}