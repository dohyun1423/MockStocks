# MockStock / StockVault

MockStock / StockVault는 실제 주식 거래 플랫폼의 구조를 참고해 만든 모의투자 웹 애플리케이션입니다. 단순 CRUD가 아니라 인증, 관심종목, 종목 조회, 주문, 보유종목, 포트폴리오, 거래내역 흐름을 하나의 서비스 흐름으로 연결하는 것을 목표로 합니다.

현재 프로젝트는 개발 진행 중인 중간 단계입니다. 핵심 거래 흐름과 KIS 한국투자증권 Open API 연동 구조는 구현되어 있으며, 실시간 WebSocket 차트와 호가 고도화는 추후 단계로 남아 있습니다.

---

## 목차

- [프로젝트 개요](#프로젝트-개요)
- [개발 배경](#개발-배경)
- [기술 스택](#기술-스택)
- [주요 기능](#주요-기능)
- [요구사항 명세](#요구사항-명세)
- [시스템 구조](#시스템-구조)
- [인증 구조](#인증-구조)
- [설치 및 실행 방법](#설치-및-실행-방법)
- [사용 방법](#사용-방법)
- [API 명세](#api-명세)
- [트러블슈팅](#트러블슈팅)
- [테스트](#테스트)
- [프로젝트 상태 및 로드맵](#프로젝트-상태-및-로드맵)
- [기여 방법](#기여-방법)
- [팀원](#팀원)
- [라이선스](#라이선스)

---

## 프로젝트 개요

### 무엇을 만들었는지

StockVault는 사용자가 가상의 현금을 기반으로 국내 주식을 검색하고, 관심종목을 관리하며, 현재가 기준으로 매수/매도 주문을 수행할 수 있는 모의투자 서비스입니다.

주요 흐름은 다음과 같습니다.

```text
회원가입/로그인
-> 종목 검색
-> 관심종목 등록 및 정렬
-> 종목 상세 조회
-> 현재가/차트 조회
-> 매수/매도 주문
-> 보유종목 및 포트폴리오 확인
-> 거래내역 확인
```

### 왜 만들었는지

주식 거래 플랫폼은 인증, 외부 API 연동, 사용자별 자산 상태, 주문 처리, 거래내역 기록, UI 상태 동기화 등 여러 기능이 복합적으로 연결됩니다. 이 프로젝트는 그런 구조를 직접 설계하고 구현하면서 백엔드와 프론트엔드가 어떻게 함께 동작하는지 학습하기 위해 시작했습니다.

특히 다음 내용을 중점으로 다룹니다.

- JWT 기반 stateless 인증
- 사용자별 관심종목, 보유종목, 거래내역 관리
- KIS Open API를 통한 현재가 및 차트 데이터 조회
- 화면 표시용 종목명과 API 처리용 종목코드 분리
- 실제 거래 서비스에 가까운 도메인 흐름 설계

---

## 기술 스택

### Backend

- Java 21
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- MySQL
- Gradle
- Lombok

### Frontend

- Thymeleaf
- HTML
- CSS
- JavaScript
- Fetch API

### External API

- KIS 한국투자증권 Open API
  - 현재가 조회
  - 기간별 차트 조회
  - 추후 WebSocket 실시간 시세 연동 예정

---

## 주요 기능

### 인증

- 이메일, 비밀번호 기반 회원가입
- BCrypt 비밀번호 암호화
- 로그인 성공 시 JWT 발급
- 프론트에서 `localStorage.accessToken` 저장
- 보호 API 요청 시 `Authorization: Bearer {token}` 사용
- JWT subject는 사용자 email
- `Authentication.getName()`은 로그인 사용자 email

### 종목 검색 및 상세 조회

- 종목명 또는 종목코드로 검색
- 검색 결과는 `symbol`, `name`, `market` 포함
- 검색 결과 클릭 시 종목명 대신 `symbol` 기준으로 상세페이지 이동
- 상세페이지에서는 `symbol` 우선 조회

### 관심종목

- 사용자별 관심종목 등록/삭제
- 관심종목 목록 조회
- 관심종목 순서 변경
- 메인 페이지 진입 시 첫 번째 관심종목 자동 선택
- 현재 DB 구조는 `stockName` 저장 방식 유지
- 응답에는 가능한 경우 `symbol`, `market` 포함

### 현재가 및 차트

- `/api/stocks/{symbol}/quote`로 현재가 조회
- KIS 현재가 API는 반드시 `symbol` 기준 호출
- `/api/stocks/{symbol}/prices?period=...`로 차트 데이터 조회
- 현재 차트 기간 매핑

```text
1D -> 최근 1일 + 일봉 fallback
1W -> 최근 1주 + 일봉
1M -> 최근 1개월 + 일봉
1Y -> 최근 1년 + 월봉
```

`1D`의 진짜 실시간 분봉/틱 차트는 WebSocket 또는 분봉 API 연동 후 고도화할 예정입니다.

### 주문

- 현재가 기준 즉시 매수
- 현재가 기준 즉시 매도
- 매수 시 현금 검증
- 매도 시 보유수량 검증
- 주문 성공 시 보유종목 갱신
- 모든 주문은 거래내역으로 저장

### 포트폴리오

- 현금 잔고 조회
- 총자산, 평가금액, 매입금액, 손익, 수익률 조회
- 보유종목 목록 조회
- 보유종목별 현재가, 평가금액, 손익, 수익률 계산
- KIS 현재가 조회 실패 시 DB 저장 가격 fallback 처리

### 거래내역

- 전체 거래내역 조회
- 특정 종목 `symbol` 기준 거래내역 조회
- 상세페이지의 내 주식 탭에서 현재 종목 거래내역 표시

---

## 요구사항 명세

### 기능 요구사항

| 구분 | 요구사항 | 현재 상태 |
| --- | --- | --- |
| 회원 | 사용자는 이메일, 비밀번호, 닉네임으로 가입할 수 있다. | 구현 |
| 인증 | 로그인 성공 시 JWT를 발급한다. | 구현 |
| 인증 | 보호 API는 Bearer Token을 검증한다. | 구현 |
| 종목 | 사용자는 종목명 또는 종목코드로 종목을 검색할 수 있다. | 구현 |
| 종목 | 상세 조회와 API 매칭은 종목코드 `symbol`을 우선 사용한다. | 구현 |
| 관심종목 | 사용자는 관심종목을 등록, 조회, 삭제할 수 있다. | 구현 |
| 관심종목 | 사용자는 관심종목 순서를 변경할 수 있다. | 구현 |
| 차트 | 사용자는 기간별 차트를 조회할 수 있다. | 구현 중 |
| 주문 | 사용자는 현재가 기준 매수/매도 주문을 할 수 있다. | 구현 |
| 포트폴리오 | 사용자는 현금, 보유종목, 평가손익을 확인할 수 있다. | 구현 |
| 거래내역 | 사용자는 전체 또는 종목별 거래내역을 조회할 수 있다. | 구현 |
| 호가 | 사용자는 종목별 호가를 확인할 수 있다. | 예정 |
| 실시간 | 실시간 시세와 차트를 WebSocket으로 갱신한다. | 예정 |

### 비기능 요구사항

- 인증은 JWT 기반 stateless 구조를 유지한다.
- 세션 인증은 사용하지 않는다.
- 화면 표시는 `stockName` 또는 `name`을 사용한다.
- API 호출, 비교, 매칭은 `symbol`을 사용한다.
- KIS API 호출은 종목코드 `symbol` 기준으로 수행한다.
- 민감정보는 `application-secret.yml` 또는 환경변수로 분리한다.
- Controller는 요청/응답을 담당하고, 핵심 비즈니스 로직은 Service 계층에 둔다.
- Entity를 직접 응답하지 않고 DTO를 사용한다.

---

## 시스템 구조

### 주요 도메인

```text
User
- email
- password
- nickname
- role
- cash

Stock
- symbol
- name
- market
- sector
- currentPrice
- changePrice
- changeRate
- volume
- marketCap
- listedShares
- per
- eps
- dividendYield

Watchlist
- user
- stockName
- sortOrder

Holding
- user
- stock
- quantity
- averagePrice

Trade
- user
- stock
- orderType
- quantity
- price
- totalAmount
- createdAt
```

### 패키지 구조

```text
src/main/java/com/stock/mockstock
├─ domain
│  ├─ user
│  ├─ stock
│  ├─ watchlist
│  ├─ order
│  └─ portfolio
├─ global
│  ├─ config
│  ├─ exception
│  ├─ response
│  └─ security
└─ web
   └─ controller
```

### 화면 구조

```text
/login          로그인
/signup         회원가입
/main           메인 화면
/stocks/detail  종목 상세 화면
/portfolio      포트폴리오 화면
```

메인 화면은 다음 흐름을 중심으로 구성됩니다.

- 관심종목 탭
  - 왼쪽: 관심종목 목록
  - 가운데: 선택 종목 차트
  - 오른쪽: 선택 종목 상세 정보
- 내 주식 탭
  - 포트폴리오 요약
  - 보유종목
  - 거래내역

상세페이지는 다음 탭을 가집니다.

- 차트
- 호가
- 내 주식
- 종목정보

---

## 인증 구조

### 로그인

```http
POST /api/users/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password"
}
```

로그인 성공 시 JWT를 반환합니다.

```json
{
  "token": "jwt-access-token"
}
```

프론트는 토큰을 `localStorage`에 저장합니다.

```javascript
localStorage.setItem('accessToken', data.token);
```

### 인증 요청

보호 API 요청에는 다음 헤더를 포함합니다.

```http
Authorization: Bearer {accessToken}
```

JWT subject는 email이며, Spring Security 인증 객체에서는 다음처럼 사용합니다.

```java
authentication.getName()
```

이 값은 종목명이 아니라 로그인 사용자 email입니다.

---

## 설치 및 실행 방법

### 사전 요구사항

- Java 21
- MySQL 8.x
- Gradle Wrapper 사용 가능 환경
- KIS Open API App Key / App Secret

### 1. 저장소 클론

```bash
git clone {repository-url}
cd mockstock_trading
```

### 2. MySQL 데이터베이스 생성

```sql
CREATE DATABASE mockstock CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 환경설정 파일 작성

민감정보는 커밋하지 않습니다. 로컬에서 `src/main/resources/application-secret.yml`을 생성해 사용합니다.

```yaml
spring:
  datasource:
    username: root
    password: your-db-password

kis:
  base-url: https://openapi.koreainvestment.com:9443
  app-key: your-kis-app-key
  app-secret: your-kis-app-secret
  provider: kis
```

KIS API 없이 더미 데이터로 실행하려면 다음처럼 설정할 수 있습니다.

```yaml
kis:
  provider: dummy
```

`application.yml`에는 민감정보를 직접 넣지 않는 것을 권장합니다.

```yaml
spring:
  profiles:
    include: secret
```

### 4. 빌드

```bash
./gradlew build
```

Windows CMD 또는 PowerShell에서는 다음 명령을 사용할 수 있습니다.

```cmd
gradlew.bat build
```

### 5. 실행

```bash
./gradlew bootRun
```

Windows:

```cmd
gradlew.bat bootRun
```

기본 접속 주소:

```text
http://localhost:8080
```

---

## 사용 방법

### 기본 사용 흐름

1. `/signup`에서 회원가입합니다.
2. `/login`에서 로그인합니다.
3. 로그인 성공 후 `/main`으로 이동합니다.
4. 상단 검색창에서 종목명 또는 종목코드를 검색합니다.
5. 검색 결과를 클릭하면 `/stocks/detail?keyword={symbol}`로 이동합니다.
6. 상세페이지에서 관심종목 등록, 차트 조회, 매수/매도를 진행합니다.
7. 메인 화면의 내 주식 탭에서 보유종목과 거래내역을 확인합니다.

### 종목 검색 규칙

검색 결과 클릭 시 반드시 종목명 대신 `symbol`로 상세페이지에 이동합니다.

```text
/stocks/detail?keyword=005930
```

이유는 종목명으로 다시 조회할 경우 비슷한 이름의 다른 종목이 선택될 수 있기 때문입니다.

예:

```text
현대차 검색 -> 현대차증권이 잘못 선택될 수 있음
```

따라서 검색 결과 선택 이후의 상세 조회, 주문, 포트폴리오 비교는 모두 `symbol` 기준으로 처리합니다.

---

## API 명세

인증이 필요한 API는 `Authorization: Bearer {token}` 헤더가 필요합니다.

### User API

| Method | Endpoint | 설명 | 인증 |
| --- | --- | --- | --- |
| POST | `/api/users/signup` | 회원가입 | 불필요 |
| POST | `/api/users/login` | 로그인 및 JWT 발급 | 불필요 |
| GET | `/api/users/me` | 내 정보 조회 | 필요 |

회원가입 요청:

```json
{
  "email": "user@example.com",
  "password": "password",
  "nickname": "nick"
}
```

로그인 요청:

```json
{
  "email": "user@example.com",
  "password": "password"
}
```

### Stock API

| Method | Endpoint | 설명 | 인증 |
| --- | --- | --- | --- |
| GET | `/api/stocks/search?keyword={keyword}` | 종목명/종목코드 검색 | 필요 |
| GET | `/api/stocks/id/{stockId}` | 종목 ID 기준 상세 조회 | 필요 |
| GET | `/api/stocks/detail?name={name}` | 종목명 기준 상세 조회 | 필요 |
| GET | `/api/stocks/symbol/{symbol}` | 종목코드 기준 상세 조회 | 필요 |
| GET | `/api/stocks/{symbol}/quote` | KIS 현재가 조회 | 필요 |
| GET | `/api/stocks/{symbol}/prices?period=1D` | 차트 데이터 조회 | 필요 |

검색 응답 예시:

```json
[
  {
    "id": 1,
    "symbol": "005930",
    "name": "삼성전자",
    "market": "KOSPI"
  }
]
```

차트 기간 값:

```text
1D, 1W, 1M, 1Y
```

현재 구현 기준 KIS 요청 매핑:

```text
1D -> 최근 1일 + 일봉 fallback
1W -> 최근 1주 + 일봉
1M -> 최근 1개월 + 일봉
1Y -> 최근 1년 + 월봉
```

### Watchlist API

| Method | Endpoint | 설명 | 인증 |
| --- | --- | --- | --- |
| GET | `/api/watchlists` | 내 관심종목 목록 조회 | 필요 |
| POST | `/api/watchlists` | 관심종목 추가 | 필요 |
| PATCH | `/api/watchlists/order` | 관심종목 순서 변경 | 필요 |
| DELETE | `/api/watchlists?stockName={stockName}` | 관심종목 삭제 | 필요 |

관심종목 추가 요청:

```json
{
  "stockName": "삼성전자"
}
```

관심종목 순서 변경 요청:

```json
{
  "watchlistIds": [3, 1, 2]
}
```

응답 예시:

```json
[
  {
    "id": 1,
    "stockName": "삼성전자",
    "symbol": "005930",
    "market": "KOSPI",
    "sortOrder": 1
  }
]
```

### Order API

| Method | Endpoint | 설명 | 인증 |
| --- | --- | --- | --- |
| POST | `/api/orders/buy` | 현재가 기준 매수 | 필요 |
| POST | `/api/orders/sell` | 현재가 기준 매도 | 필요 |

주문 요청:

```json
{
  "symbol": "005930",
  "quantity": 10
}
```

주문 응답:

```json
{
  "stockName": "삼성전자",
  "orderType": "BUY",
  "quantity": 10,
  "price": 71400,
  "totalAmount": 714000,
  "cashBalance": 9286000
}
```

### Portfolio API

| Method | Endpoint | 설명 | 인증 |
| --- | --- | --- | --- |
| GET | `/api/portfolio` | 내 포트폴리오 조회 | 필요 |

응답 구조:

```json
{
  "cashBalance": 9000000,
  "totalAsset": 10000000,
  "totalEvaluation": 1000000,
  "totalPurchaseAmount": 950000,
  "totalProfitLoss": 50000,
  "totalProfitRate": 5.26,
  "holdings": [
    {
      "stockName": "삼성전자",
      "symbol": "005930",
      "quantity": 10,
      "averagePrice": 70000,
      "currentPrice": 71400,
      "evaluationAmount": 714000,
      "profitLoss": 14000,
      "profitRate": 2.0
    }
  ]
}
```

### Trade API

| Method | Endpoint | 설명 | 인증 |
| --- | --- | --- | --- |
| GET | `/api/trades` | 내 전체 거래내역 조회 | 필요 |
| GET | `/api/trades?symbol={symbol}` | 특정 종목 거래내역 조회 | 필요 |

### Admin API

| Method | Endpoint | 설명 | 인증 |
| --- | --- | --- | --- |
| POST | `/api/admin/stocks/master/import` | KOSPI/KOSDAQ MST 종목 마스터 import | 필요 |

---

## 트러블슈팅

### 1. application.yml 또는 application-secret.yml 민감정보 노출

DB 비밀번호, KIS App Key, App Secret은 절대 커밋하지 않습니다.

권장 방식:

```text
application.yml          -> 공통 설정
application-secret.yml   -> 로컬 민감정보
```

이미 노출된 키는 재발급하거나 변경해야 합니다.

### 2. `/api/portfolio`가 간헐적으로 401 또는 403을 반환하는 문제

원인 후보:

- 로그인 체크가 끝나기 전에 보호 API가 먼저 호출됨
- 만료되었거나 잘못된 JWT가 `localStorage`에 남아 있음
- 보호 API 호출 전 인증 준비 상태를 기다리지 않음

해결 방향:

- `header.js`에서 `window.authReady = checkLoginStatus()` 흐름 사용
- 보호 API 호출 전 `waitAuthReady()` 적용
- 401/403 발생 시 토큰 삭제 후 `/login`으로 이동

### 3. 종목명으로 상세 이동 시 다른 종목이 선택되는 문제

원인:

- 종목명이 비슷한 경우 이름 검색 결과가 의도와 다를 수 있음

해결:

- 검색 결과 클릭 후 상세 이동은 반드시 `symbol` 기준으로 처리

```text
/stocks/detail?keyword=005930
```

### 4. 차트 기간이 실제 선택 기간보다 길게 조회되는 문제

원인:

- 기존 KIS 차트 요청에서 `1D -> 1개월`, `1W -> 3개월`, `1M -> 1년`, `1Y -> 5년`처럼 시작일을 크게 잡고 있었음

해결:

- 현재는 다음 기준으로 조정

```text
1D -> 최근 1일 + 일봉 fallback
1W -> 최근 1주 + 일봉
1M -> 최근 1개월 + 일봉
1Y -> 최근 1년 + 월봉
```

향후 개선:

- `1D`는 KIS WebSocket 또는 분봉 API로 실시간/장중 차트 구현

### 5. KIS 현재가 조회 실패 시 포트폴리오 전체가 깨지는 문제

원인:

- 보유종목 중 하나의 KIS 현재가 조회가 실패하면 포트폴리오 계산이 중단될 수 있음

해결:

- 현재가 조회 실패 시 DB 저장 가격을 fallback으로 사용
- 실패 종목은 로그로 확인

---

## 테스트

현재 자동화 테스트는 기본 Spring Boot context 테스트 중심이며, 주요 기능은 수동 테스트와 컴파일 검증을 병행하고 있습니다.

### 테스트 실행

```bash
./gradlew test
```

Windows:

```cmd
gradlew.bat test
```

### 컴파일 확인

```bash
./gradlew compileJava
```

Windows:

```cmd
gradlew.bat compileJava
```

### 현재 수동 확인 항목

- 회원가입/로그인
- JWT 저장 및 인증 API 호출
- 메인 페이지 진입
- 관심종목 등록/삭제/정렬
- 첫 번째 관심종목 자동 선택
- 종목 검색 후 symbol 기준 상세 이동
- KIS 현재가 조회
- 차트 기간 조회
- 매수/매도 주문
- 주문 후 보유종목, 포트폴리오, 거래내역 갱신
- 상세페이지 내 주식 탭에서 해당 종목 보유정보/거래내역 표시

---

## 프로젝트 상태 및 로드맵

### 현재 상태

```text
상태: 개발 중
단계: 핵심 모의투자 흐름 구현 및 UI/API 안정화
```

### 완료된 주요 작업

- JWT 인증 구조
- 회원가입/로그인
- 공통 헤더와 로그인 상태 확인
- 종목 검색 및 상세 이동
- KIS 현재가 조회 provider
- KIS 차트 조회 provider
- Dummy provider fallback 구조
- 관심종목 등록/삭제/정렬
- 메인 관심종목 첫 항목 자동 선택
- 주문 모달
- 매수/매도 주문
- 보유종목 관리
- 포트폴리오 조회
- 거래내역 조회
- 상세페이지 차트/내 주식/종목정보 탭

### 진행 예정

- 1D 차트 WebSocket 또는 분봉 API 연동
- KIS 호가 API 또는 Dummy 호가 provider 설계
- 상세페이지 호가 탭 구현
- 관심종목 DB 구조 개선
  - 현재: `user_id + stockName`
  - 목표: `user_id + stock_id`
- 내정보 모달 고도화
  - 이메일/닉네임 표시
  - 닉네임 변경
  - 비밀번호 변경
- 테스트 코드 확장
- 예외 응답 포맷 통일
- UI 반응형 개선

---

## 기여 방법

현재 프로젝트는 개발 중이므로 기능 추가 전 이슈 또는 작업 내용을 먼저 정리하는 것을 권장합니다.

### 브랜치 전략 예시

```text
main
feature/chart-api
feature/order-flow
fix/auth-ready
fix/portfolio-fallback
```

### Pull Request 가이드

PR에는 다음 내용을 포함합니다.

```text
## Summary
- 변경 내용 요약

## Test
- 실행한 테스트
- 확인한 화면/기능

## Notes
- 리뷰어가 알아야 할 내용
```

### 코드 스타일

- Spring Boot conventions 사용
- Controller는 얇게 유지
- 비즈니스 로직은 Service 계층에 작성
- Entity 직접 응답 금지, DTO 사용
- 생성자 주입 사용
- 민감정보 커밋 금지
- 화면 표시는 stockName/name
- API 호출, 비교, 매칭은 symbol
- 새 기능 또는 복잡한 로직에는 간단한 주석 추가
