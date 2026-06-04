// 사용자의 현금과 보유 주식 평가 정보를 조회하는 서비스
package com.stock.mockstock.domain.portfolio.service;

import com.stock.mockstock.domain.portfolio.dto.HoldingResponse;
import com.stock.mockstock.domain.portfolio.dto.PortfolioResponse;
import com.stock.mockstock.domain.portfolio.entity.Holding;
import com.stock.mockstock.domain.portfolio.repository.HoldingRepository;
import com.stock.mockstock.domain.stock.dto.StockQuoteResponse;
import com.stock.mockstock.domain.stock.service.StockQuoteService;
import com.stock.mockstock.domain.user.entity.User;
import com.stock.mockstock.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;
    private final StockQuoteService stockQuoteService;

    // 내 현금과 보유 주식 목록 조회
    public PortfolioResponse getMyPortfolio(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<HoldingResponse> holdings = holdingRepository.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toHoldingResponse)
                .toList();

        return new PortfolioResponse(user.getCash(), holdings);
    }

    private HoldingResponse toHoldingResponse(Holding holding) {
        StockQuoteResponse quote = stockQuoteService.getQuote(holding.getStock().getSymbol());

        Long currentPrice = quote.getCurrentPrice();
        Long evaluationAmount = currentPrice * holding.getQuantity();
        Long purchaseAmount = holding.getAveragePrice() * holding.getQuantity();
        Long profitLoss = evaluationAmount - purchaseAmount;
        BigDecimal profitRate = calculateProfitRate(profitLoss, purchaseAmount);

        return new HoldingResponse(
                holding.getStock().getName(),
                holding.getStock().getSymbol(),
                holding.getQuantity(),
                holding.getAveragePrice(),
                currentPrice,
                evaluationAmount,
                profitLoss,
                profitRate
        );
    }

    private BigDecimal calculateProfitRate(Long profitLoss, Long purchaseAmount) {
        if (purchaseAmount == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(profitLoss)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(purchaseAmount), 2, RoundingMode.HALF_UP);
    }
}