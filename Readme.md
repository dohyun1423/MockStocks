# MockStock

MockStock은 실제 증권 플랫폼 구조를 참고하여 만드는 모의 주식 투자 플랫폼입니다.

현재는 인증, JWT 흐름, Thymeleaf 기반 UI 연결, 관심종목 저장, 주식 상세 화면 구조를 중심으로 개발 중입니다.

---

# Tech Stack

## Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- JWT
- MySQL
- Gradle

## Frontend

- Thymeleaf
- HTML
- CSS
- JavaScript
- Fetch API

---

# Authentication Flow

## Login

1. 사용자가 이메일과 비밀번호로 로그인합니다.
2. 서버에서 비밀번호를 검증합니다.
3. 검증 성공 시 JWT Access Token을 발급합니다.
4. 프론트는 토큰을 `localStorage`에 저장합니다.

```javascript
localStorage.setItem('accessToken', data.token);
```

## Request

인증이 필요한 API 요청에는 아래 헤더를 포함합니다.

```text
Authorization: Bearer {accessToken}
```

## Token Expiration

현재 Access Token 만료 시간은 1시간입니다.

```java
private static final long ACCESS_TOKEN_EXPIRATION_TIME = 60 * 60 * 1000L;
```

탭을 닫아도 `localStorage`에 토큰이 남아 있으면 다시 접속 시 로그인 상태가 유지됩니다.

로그아웃 시에는 토큰을 삭제합니다.

```javascript
localStorage.removeItem('accessToken');
```

---

# Current Pages

## Login Page

```text
/login
```

- 이메일/비밀번호 로그인
- JWT 발급
- 토큰을 `localStorage`에 저장
- 로그인 성공 시 `/main`으로 이동

## Signup Page

```text
/signup
```

- 이메일, 닉네임, 비밀번호 기반 회원가입
- 비밀번호 BCrypt 암호화
- 기본 모의 투자금 설정

## Main Page

```text
/main
```

메인 화면은 2:6:2 비율 구조입니다.

```text
왼쪽   : 관심종목 목록
가운데 : 선택된 종목 차트 영역
오른쪽 : 선택된 종목 관련 정보 영역
```

상단에는 다음 요소가 있습니다.

- 왼쪽: StockVault 로고
- 가운데: 종목 검색창
- 오른쪽: 사용자 프로필, 로그아웃 버튼

관심종목을 클릭하면 메인 화면 안에서 가운데 차트 영역과 오른쪽 종목 정보 영역이 갱신되는 구조입니다.

## Stock Detail Page

```text
/stocks/detail?keyword={종목명}
```

검색창에서 종목명을 입력하면 상세 페이지로 이동합니다.

상세 화면 구조:

- 종목명
- 현재가
- 어제대비
- 등락률
- 거래량
- 관심종목 추가 하트 버튼
- 탭 메뉴
  - 차트
  - 호가
  - 내 주식
  - 종목정보

현재는 실제 주식 데이터 없이 화면 구조와 동작 흐름만 구현하는 단계입니다.

---

# Security Notes

HTML 페이지 이동은 브라우저가 `Authorization` 헤더를 자동으로 붙이지 않습니다.

따라서 Thymeleaf 화면 URL은 접근을 허용하고, 실제 데이터 API는 JWT 인증을 요구하는 방식으로 구성합니다.

예시:

```java
.requestMatchers(
        "/api/users/signup",
        "/api/users/login",
        "/stocks/detail"
).permitAll()
.anyRequest().authenticated()
```

실제 보호 대상은 API입니다.

```text
/api/users/me
/api/watchlists
```

---

# Watchlist

관심종목은 사용자별로 유지되어야 하므로 DB에 저장합니다.

현재 구조는 실제 `Stock` 엔티티가 없는 단계이므로, 우선 사용자와 종목명을 연결하는 방식입니다.

## Table Concept

```text
watchlists
- id
- user_id
- stock_name
- created_at
- updated_at
```

중복 방지를 위해 아래 조합에 Unique 제약을 둡니다.

```text
user_id + stock_name
```

## API

```text
POST /api/watchlists
관심종목 추가
```

```text
GET /api/watchlists
내 관심종목 목록 조회
```

```text
DELETE /api/watchlists?stockName={종목명}
관심종목 삭제
```

## Frontend Flow

상세 페이지에서 하트 버튼 클릭:

```text
POST /api/watchlists
```

메인 페이지 접속:

```text
GET /api/watchlists
```

응답받은 관심종목 목록을 왼쪽 관심종목 영역에 표시합니다.

---

# Current Package Structure

```text
src/main/java/com/stock/mockstock
```

```text
global
- config
- entity
- exception
- response
- security

domain
- user
  - controller
  - dto
  - entity
  - enumtype
  - repository
  - service

- watchlist
  - controller
  - dto
  - entity
  - repository
  - service

web
- controller
```

---

# Important Files

## Backend

```text
global/config/SecurityConfig.java
global/security/jwt/JwtUtil.java
global/security/jwt/JwtAuthenticationFilter.java
web/controller/ViewController.java
domain/user/controller/UserController.java
domain/user/service/UserService.java
domain/watchlist/controller/WatchlistController.java
domain/watchlist/service/WatchlistService.java
```

## Frontend

```text
templates/login.html
templates/signup.html
templates/main.html
templates/stock_detail.html

static/js/login.js
static/js/signup.js
static/js/main.js
static/js/stock_detail.js

static/css/login.css
static/css/signup.css
static/css/main.css
static/css/stock_detail.css
```

---

# Current Development Status

## Done

- 회원가입 API
- 로그인 API
- JWT 발급
- JWT 인증 필터
- BCrypt 비밀번호 암호화
- 로그인/회원가입 UI
- 메인 화면 기본 구조
- 주식 상세 화면 기본 구조
- 검색 시 상세 페이지 이동
- 관심종목 DB 저장 구조
- 관심종목 추가 API
- 관심종목 목록 조회 API
- 메인 화면 관심종목 표시 구조

## In Progress

- 관심종목 클릭 시 메인 차트 영역 갱신
- 관심종목 클릭 시 오른쪽 종목 정보 영역 갱신
- 실제 주식 데이터 연결 전 화면 구조 정리

## Next Goals

- Stock 엔티티 설계
- 실제 종목 검색 API 구현
- 종목 상세 데이터 API 구현
- 차트 데이터 구조 설계
- 호가 화면 구현
- 내 주식 보유 정보 구현
- 포트폴리오 시스템 구현
- 주문 시스템 구현
```