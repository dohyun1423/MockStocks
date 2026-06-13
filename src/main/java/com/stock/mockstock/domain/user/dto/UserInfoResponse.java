// 현재 로그인한 사용자 정보를 내려주는 응답 DTO
package com.stock.mockstock.domain.user.dto;

import com.stock.mockstock.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {

    private String email;
    private String nickname;

    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
                user.getEmail(),
                user.getNickname()
        );
    }
}