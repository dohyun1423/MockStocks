// 보유 주식 DB 조회를 담당하는 Repository
package com.stock.mockstock.domain.portfolio.repository;

import com.stock.mockstock.domain.portfolio.entity.Holding;
import com.stock.mockstock.domain.stock.entity.Stock;
import com.stock.mockstock.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

    Optional<Holding> findByUserAndStock(User user, Stock stock);

    List<Holding> findAllByUserOrderByCreatedAtDesc(User user);
}