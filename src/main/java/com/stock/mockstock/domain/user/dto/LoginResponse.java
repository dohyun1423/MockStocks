// 로그인 성공 시 JWT를 반환하는 응답 DTO
package com.stock.mockstock.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String token;
}
