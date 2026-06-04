// Thymeleaf 화면 요청을 처리하는 컨트롤러
package com.stock.mockstock.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

    // 루트 주소 접속 시 localStorage 토큰 확인 페이지로 이동
    @GetMapping("/")
    public String indexPage() {
        return "index";
    }

    // 로그인 화면 이동
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // 회원가입 화면 이동
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    // 메인 화면 이동
    @GetMapping("/main")
    public String mainPage() {
        return "main";
    }

    // 검색한 종목 상세 화면 이동
    @GetMapping("/stocks/detail")
    public String stockDetailPage(@RequestParam String keyword, Model model) {
        model.addAttribute("keyword", keyword);
        return "stock_detail";
    }

    // 개인 포트폴리오 화면 이동
    @GetMapping("/portfolio")
    public String portfolioPage() {
        return "portfolio";
    }
}
