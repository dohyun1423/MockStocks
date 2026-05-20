# MockStock

실시간 주식 데이터를 기반으로 하는 모의 투자 플랫폼 프로젝트.

Spring Boot 기반으로 개발 중이며,
JWT 인증 시스템과 실제 증권 플랫폼 스타일 구조를 목표로 한다.

---

# Tech Stack

## Backend
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- JWT

## Database
- MySQL

## Frontend
- Thymeleaf
- HTML/CSS
- JavaScript

## Build Tool
- Gradle

---

# Current Features

## Authentication
- 회원가입
- 로그인
- BCrypt 비밀번호 암호화
- JWT 발급
- JWT 인증 필터

## Security
- Spring Security 적용
- Stateless JWT 인증 구조
- Global Exception Handling

## UI
- 회원가입 페이지
- 로그인 페이지
- 메인 페이지 구조

---

# Project Structure

src/main/java/com/stock/mockstock

global
- config
- exception
- response
- security

domain
- user
    - controller
    - dto
    - entity
    - repository
    - service

web
- controller

---

# Running

## MySQL 실행

MySQL 서버 실행 후:

database 생성:

CREATE DATABASE mockstock;

---

## application.yml 설정

spring:
datasource:
url: jdbc:mysql://localhost:3306/mockstock
username: root
password: 비밀번호

jwt:
secret: your-secret-key

---

## 실행

./gradlew bootRun

또는 IntelliJ 실행 버튼 사용.

---

# Current Development Status

현재 구현 완료:
- Signup API
- Login API
- JWT Authentication
- Security Filter
- Thymeleaf UI

다음 목표:
- 로그인 유지
- 관심 종목
- 주식 도메인
- 포트폴리오
- 주문 시스템