// 회원가입 요청을 받는 입구
package com.stock.mockstock.domain.user.controller;

import com.stock.mockstock.domain.user.dto.SignupRequest;
import com.stock.mockstock.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.stock.mockstock.domain.user.dto.LoginRequest;
import com.stock.mockstock.domain.user.dto.LoginResponse;
import org.springframework.security.core.Authentication;

@RestController
@RequiredArgsConstructor // lombok의 기능
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService; // lombok의 기능으로 자동 생성자 주입

    @PostMapping("/signup")
    public String signup(@RequestBody @Valid SignupRequest request) { // RequestBody = client json요청을 객체로 변환

        userService.signup(request); // 처리 호출

        return "회원가입 성공";
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {

        String token = userService.login(request);

        return new LoginResponse(token);
    }

    @GetMapping("/me")
    public String me(Authentication authentication) {

        return authentication.getName();
    }
}