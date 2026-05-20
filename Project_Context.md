# MockStock Development Context

## Project Goal

실제 증권 플랫폼 구조를 참고한
모의 주식 투자 웹 서비스 개발.

단순 CRUD가 아니라:

- 인증 시스템
- 자산 관리
- 관심 종목
- 실시간 데이터
- 포트폴리오
- 거래 시스템

구현을 목표로 한다.

---

# Architecture

## Backend Architecture

Spring Boot MVC 기반.

구조:

Controller
→ Service
→ Repository
→ Entity

---

# Authentication Flow

## Signup

사용자 회원가입 시:

1. 이메일 중복 검사
2. 닉네임 중복 검사
3. BCrypt 암호화
4. DB 저장

---

## Login

로그인 시:

1. 이메일 조회
2. 비밀번호 검증
3. JWT 생성
4. 토큰 반환

---

# JWT Structure

현재 JWT에는:

- email(subject)
- issuedAt
- expiration

포함.

---

# Security

Spring Security + JWT 기반 인증 사용.

현재:
- /signup permitAll
- /login permitAll
- 나머지 authenticated

세션 사용 안 함.

STATELESS 구조.

---

# Global Exception Handling

@RestControllerAdvice 기반.

현재 IllegalArgumentException 처리 중.

응답 형식:

{
"message": "에러메시지"
}

---

# Current UI Status

## signup.html
- 실제 회원가입 API 연결 완료
- fetch 기반 API 호출
- validation 구현
- signup.css 분리 완료

## login.html
- 로그인 API 연동 완료
- JWT localStorage 저장
- 로그인 성공 시 /main 이동

---

# Current Package Structure

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

# Coding Style

- Controller는 최대한 얇게 유지
- 비즈니스 로직은 Service에서 처리
- DTO 사용
- Entity 직접 반환 금지
- 예외는 Global Handler 사용
- JWT 기반 인증 유지
- RESTful API 스타일 유지

---

# Current Main Entities

## User

fields:
- id
- email
- password
- nickname
- cash

---

# Planned Features

## Stock Domain
- Stock Entity
- 실시간 가격
- 거래량
- 상승률
- 관심 종목

## Trading
- 매수
- 매도
- 예약 주문

## Portfolio
- 보유 종목
- 총 자산
- 수익률
- 비율 계산

---

# Immediate Next Goals

1. JWT 인증 유지
2. main 페이지 인증 적용
3. 현재 로그인 사용자 표시
4. 관심 종목 기능
5. Stock Entity 설계

---

# Important Notes

현재는 개발 초기 단계이며
Entity/구조는 이후 변경 가능.

우선:
- 인증
- 구조
- 흐름
  중심으로 개발 진행 중.