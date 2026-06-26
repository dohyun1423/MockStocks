package com.stock.mockstock.domain.order.repository;

import com.stock.mockstock.domain.order.entity.PendingOrder;
import com.stock.mockstock.domain.order.enumtype.PendingOrderStatus;
import com.stock.mockstock.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PendingOrderRepository extends JpaRepository<PendingOrder, Long> {

    List<PendingOrder> findAllByStatusOrderByCreatedAtAsc(PendingOrderStatus status);

    List<PendingOrder> findAllByUserAndStatusOrderByCreatedAtDesc(
            User user,
            PendingOrderStatus status
    );
}