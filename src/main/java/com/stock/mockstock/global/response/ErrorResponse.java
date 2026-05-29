// 에러 응답 메시지를 담는 공통 응답 객체
package com.stock.mockstock.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private String message;
}
