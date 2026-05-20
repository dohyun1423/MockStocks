package com.stock.mockstock.domain.user.repository;

import com.stock.mockstock.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional; // null 안전 처리 위함

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email); // db 자동 조회

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}