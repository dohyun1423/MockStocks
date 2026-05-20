package com.stock.mockstock.global.entity; // 기본 엔티티 생성

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 상속 전용
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate // 생성 시간 저장
    private LocalDateTime createdAt;

    @LastModifiedDate // 수정 시간 저장
    private LocalDateTime updatedAt;
}