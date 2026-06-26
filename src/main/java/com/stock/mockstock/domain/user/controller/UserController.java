// 회원가입, 로그인, 내 정보 API 요청을 받는 입구
package com.stock.mockstock.domain.user.controller;

import com.stock.mockstock.domain.user.dto.SignupRequest;
import com.stock.mockstock.domain.user.service.UserService;
import com.stock.mockstock.domain.user.dto.UserInfoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.stock.mockstock.domain.user.dto.LoginRequest;
import com.stock.mockstock.domain.user.dto.LoginResponse;
import org.springframework.security.core.Authentication;
import com.stock.mockstock.domain.user.dto.TokenRefreshResponse;
import com.stock.mockstock.global.security.jwt.JwtUtil;

@RestController
@RequiredArgsConstructor // lombok의 기능
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService; // lombok의 기능으로 자동 생성자 주입
    private final JwtUtil jwtUtil;

    // 회원가입 요청 처리
    @PostMapping("/signup")
    public String signup(@RequestBody @Valid SignupRequest request) { // RequestBody = client json요청을 객체로 변환

        userService.signup(request); // 처리 호출

        return "회원가입 성공";
    }

    // 로그인 성공 시 JWT 반환
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {

        String token = userService.login(request);

        return new LoginResponse(token);
    }

    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public UserInfoResponse me(Authentication authentication) {
        return userService.getMyInfo(authentication.getName());
    }

    // 로그인 사용자가 직접 연장 버튼을 눌렀을 때 새 JWT를 발급한다.
    @PostMapping("/refresh")
    public TokenRefreshResponse refresh(Authentication authentication) {
        String token = jwtUtil.generateToken(authentication.getName());

        return new TokenRefreshResponse(token);
    }
}
