// 관심종목 DB 조회를 담당하는 Repository
package com.stock.mockstock.domain.watchlist.repository;

import com.stock.mockstock.domain.user.entity.User;
import com.stock.mockstock.domain.watchlist.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findAllByUserOrderByCreatedAtDesc(User user);

    boolean existsByUserAndStockName(User user, String stockName);

    void deleteByUserAndStockName(User user, String stockName);
}
