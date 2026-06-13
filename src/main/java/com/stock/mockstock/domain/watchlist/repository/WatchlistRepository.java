package com.stock.mockstock.domain.watchlist.repository;

import com.stock.mockstock.domain.user.entity.User;
import com.stock.mockstock.domain.watchlist.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    @Query("""
            select w
            from Watchlist w
            where w.user = :user
            order by coalesce(w.sortOrder, 999999), w.createdAt asc
            """)
    List<Watchlist> findAllByUserOrderBySortOrder(User user);

    @Query("""
            select max(w.sortOrder)
            from Watchlist w
            where w.user = :user
            """)
    Integer findMaxSortOrderByUser(User user);

    boolean existsByUserAndStockName(User user, String stockName);

    void deleteByUserAndStockName(User user, String stockName);
}