// 로그인한 사용자의 거래내역 조회를 담당하는 서비스
package com.stock.mockstock.domain.order.service;

import com.stock.mockstock.domain.order.dto.TradeResponse;
import com.stock.mockstock.domain.order.repository.TradeRepository;
import com.stock.mockstock.domain.user.entity.User;
import com.stock.mockstock.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {

    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;

    // 내 거래내역 목록 조회
    public List<TradeResponse> getMyTrades(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return tradeRepository.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(TradeResponse::from)
                .toList();
    }
}