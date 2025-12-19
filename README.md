# 월렛 동시 출금 시스템

동시성 제어와 멱등성을 보장하는 월렛 출금 REST API 시스템

## 목차
- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [프로젝트 실행 방법](#프로젝트-실행-방법)
- [API 명세](#api-명세)
- [설계 결정](#설계-결정)
- [테스트 결과](#테스트-결과)

---

## 프로젝트 개요

### 주요 기능
- 월렛 출금 API (동시성 제어)
- 멱등성 보장 (중복 요청 방지)
- 잔액 조회 API
- 거래 내역 관리

### 핵심 요구사항
✅ 100개 스레드 동시 출금 시 데이터 무결성 유지
✅ 동일 transactionId 중복 요청 시 1회만 처리
✅ 잔액 부족 시 HTTP 400 반환
✅ 모든 출금 내역 정확히 기록

---

## 기술 스택

**Backend**
- Kotlin 1.9.21
- Spring Boot 3.2.1
- Spring Data JPA

**Database**
- PostgreSQL 16
- Redis 7

**동시성 제어**
- Redisson (분산 락)
- ReentrantLock (테스트 환경)

**Build Tool**
- Gradle 8.5 (Kotlin DSL)

---

## 프로젝트 실행 방법

### 1. 사전 준비

**필수 소프트웨어**
- Java 17 이상
- Docker & Docker Compose
- Git

**설치 확인**
```bash
java -version   # 17 이상
docker --version
docker-compose --version
```

### 2. 프로젝트 클론

```bash
git clone https://github.com/ikswary/Concurrent-withdrawal-and-balance-integrity-system.git
cd Concurrent-withdrawal-and-balance-integrity-system
```

### 3. 인프라 자원 기동 (Docker Compose)

**3-1. Docker 컨테이너 시작**
```bash
docker-compose up -d
```

**3-2. 컨테이너 상태 확인**
```bash
docker ps
```

**예상 출력** (STATUS가 "healthy"여야 함):
```
CONTAINER ID   IMAGE              STATUS
abc123...      postgres:16        Up (healthy)
def456...      redis:7-alpine     Up (healthy)
```

### 4. DB 세팅

API 서버 기동 시 `application.yml`의 `ddl-auto: update` 설정으로 테이블 자동 생성됩니다.

---

## API 명세

### 1. 출금 API

**Endpoint**: `POST /api/wallets/{walletId}/withdraw`

**Request**
```json
{
  "transactionId": "TXN_UUID_12345",
  "amount": {
    "amount": 10000
  }
}
```

**Response (성공)**
```json
{
  "transactionId": "TXN_UUID_12345",
  "walletId": 1,
  "withdrawalAmount": {
    "amount": 10000
  },
  "remainingBalance": {
    "amount": 90000
  },
  "transactionTime": "2025-12-19T10:30:00"
}
```

**Response (잔액 부족)**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient balance: current=5000, requested=10000"
}
```

### 2. 잔액 조회 API

**Endpoint**: `GET /api/wallets/{walletId}/balance`

**Response**
```json
{
  "walletId": 1,
  "balance": {
    "amount": 100000
  }
}
```

---

## 설계 결정

### 동시성 제어 기법 비교: DB 비관적 락 vs Redis 분산 락

#### 검토한 두 가지 방법

**1. DB 비관적 락 (Pessimistic Lock)**
```sql
SELECT * FROM wallets WHERE id = ? FOR UPDATE
```
- DB 자체 기능으로 row-level lock
- 트랜잭션이 끝날 때까지 다른 트랜잭션 대기
- 추가 인프라 불필요

**2. Redis 분산 락**
```kotlin
redissonClient.getLock("wallet:$walletId")
```
- 외부 저장소(Redis)를 통한 락 관리
- 애플리케이션 레벨에서 제어
- Redis 인프라 필요

---

#### 왜 Redis 분산 락을 선택했나?

**DB 비관적 락을 선택하지 않은 이유**

DB Lock은 간단하고 안정적이지만, 다음 문제들이 있었습니다

**1. DB 커넥션 고갈 위험**

출금 로직이 복잡해지면 (외부 API 호출, 복잡한 검증 등) 트랜잭션이 길어집니다. 이 동안 DB 커넥션을 계속 점유하면
- 동시 요청이 많을 때 커넥션 풀이 고갈될 수 있음
- 다른 조회 쿼리까지 영향받아 전체 성능 저하

**2. 트랜잭션 경직성으로 인한 데드락 위험 증가**

DB Lock은 트랜잭션과 생명주기가 강하게 결합되어 있습니다
- 락 획득 = 트랜잭션 시작 = DB 커넥션 점유 시작
- 락 해제 = 트랜잭션 종료
- 비즈니스 로직 전체가 하나의 긴 트랜잭션이 되어 데드락 위험이 증가합니다

**Redis 분산 락을 선택한 이유**

**1. DB와 락의 분리**

가장 큰 장점은 DB 트랜잭션과 락을 독립적으로 관리할 수 있다는 점입니다
- 락은 비즈니스 로직 전체를 감싸고
- DB 트랜잭션은 실제 데이터 변경 시점만
- DB 커넥션을 최소한으로 점유

**2. 운영 시 확장성과 안정성**

트래픽이 증가 상황을 가정했을때 DB를 통한 lock의 경우 아래와 같은 문제가 발생할 가능성이 높습니다
- 3대 서버가 모두 같은 DB에 락 요청
- DB가 락 관리 + 쿼리 처리를 동시에 담당
- 락 경합이 심해질수록 DB CPU/메모리 부하 증가
- DB가 병목 → 조회 쿼리까지 느려짐
- DB 장애 = 락 + 데이터 모두 중단

**3. 고가용성 지원**

Redis는 sentinel, cluaster등 고가용성 메커니즘을 갖추어 일부 노드 장애에도 서비스를 지속할 수 있습니다

**4. 운영 관점의 장점**

- 락 관련 장애와 DB 장애를 분리해서 대응 가능
- Redis만 재시작해도 되는 상황 vs DB 전체 재시작
- 모니터링 지표 분리 가능 (락 대기 시간 vs 쿼리 성능)
---

### 성능 vs 안정성 트레이드오프

**DB 비관적 락 vs Redis 분산 락**

| 비교 항목 | DB 비관적 락 | Redis 분산 락 |
|----------|-------------|--------------|
| **높은 동시성 환경** | 커넥션 풀 고갈 위험 | 안정적 처리 |
| **DB 부하** | 높음 (락 + 쿼리) | 낮음 (쿼리만) |
| **인프라 의존성** | 낮음 (DB만) | 높음 (Redis 추가) |
| **장애 범위** | DB 장애 = 전체 중단 | Redis 장애 = 락만 중단 |
| **데이터 정합성** | ✅ 보장 | ✅ 보장 |

**동시성이 높아질수록 Redis가 유리**
- DB Lock: 락 획득부터 해제까지 DB 커넥션 점유
- 특정 wallet에 대한 버스트 트래픽 상황에서 DB의 경우 전방위적, redis의 경우 해당 wallet에 대한 지연만 발생

### 우려사항 및 향후 대책

#### 1. Redis 의존성으로 인한 가용성 저하

**문제**

Redis가 새로운 Single Point of Failure로서 병목, 시스템 장애를 일으킬 가능성 

**대응 방안**

- Redis 헬스 체크 및 자동 알림, 빠른 복구 체계 구축, Redis Cluster 구성으로 고가용성 확보

---

## 테스트 결과

### 전체 테스트 결과

**환경**
- Database: PostgreSQL 16
- Lock: Redis 7 (Redisson)
- 테스트: Spring Boot Test + JUnit 5

**결과**: ✅ 6개 테스트 모두 통과

```bash
./gradlew test --tests "*Integration*" -Dspring.profiles.active=integration

BUILD SUCCESSFUL in 14s
6 tests completed, 6 passed
```

---

### 1. 동시성 제어 적용 전후 비교

#### 테스트 시나리오
- 초기 잔액: 1,000
- 동시 스레드: 50개
- 출금액: 20 × 50회
- 예상 최종 잔액: 0

#### 테스트 결과

**락 없이 실행 (NoLockManager)**

| 항목 | 예상 | 실제 | 상태 |
|------|------|------|------|
| 최종 잔액 | 0 | 820 | ❌ 실패 |
| 거래 기록 | 50건 | 41건 | ❌ 손실 9건 |
| 데이터 무결성 | 성공 | 실패 | ❌ Lost Update |

**문제점**:
- Race Condition으로 인한 Lost Update 발생
- 9건의 거래 내역 손실
- 최종 잔액이 820으로 잘못 계산됨
- **데이터 무결성 보장 불가**

---

**락 사용 (RedisLockManager)**

| 항목 | 예상 | 실제 | 상태 |
|------|------|------|------|
| 최종 잔액 | 0 | 0 | ✅ 정확 |
| 거래 기록 | 50건 | 50건 | ✅ 완벽 |
| 데이터 무결성 | 성공 | 성공 | ✅ 보장 |

**효과**:
- 모든 출금 요청이 순차적으로 정확히 처리
- 거래 내역 손실 없음
- 최종 잔액 정확
- **데이터 무결성 완벽 보장**

---

### 2. 100 스레드 동시 출금 테스트

#### 테스트 시나리오
- 초기 잔액: 10,000
- 동시 스레드: 100개
- 출금액: 100 × 100회

#### 실행 결과

```
=== 100 Concurrent Withdrawal Test Result ===
Success: 100
Failure: 0
Final Balance: 0.00
Transaction History Count: 100
```

| 항목 | 예상 | 실제 | 상태 |
|------|------|------|------|
| 최종 잔액 | 0 | 0 | ✅ |
| 성공 건수 | 100 | 100 | ✅ |
| 실패 건수 | 0 | 0 | ✅ |
| 거래 기록 | 100 | 100 | ✅ |

**검증**: 100개 스레드 동시 요청 시에도 데이터 무결성 완벽 유지

---

### 3. 멱등성 테스트

#### 테스트 시나리오
- 초기 잔액: 10,000
- 동시 스레드: 100개
- **동일 transactionId**: "tx-idempotent-same"
- 출금액: 100

#### 실행 결과

```
=== Idempotency Test Result ===
Success: 100
Failure: 0
Final Balance: 9900.00
Transaction History Count: 1
```

| 항목 | 예상 | 실제 | 상태 |
|------|------|------|------|
| 최종 잔액 | 9,900 | 9,900 | ✅ |
| 출금 실행 | 1회 | 1회 | ✅ |
| 거래 기록 | 1건 | 1건 | ✅ |
| 중복 처리 | 기존 반환 | 기존 반환 | ✅ |

**핵심**: 동일 ID로 100번 요청해도 1번만 실행, 멱등성 완벽 보장

**구현 방식**: 락 내부에서 멱등성 체크 → Race Condition 완전 방지

---

### 4. 잔액 부족 테스트

#### 테스트 시나리오
- 초기 잔액: 5,000
- 동시 스레드: 100개
- 출금액: 100 × 100회
- 예상 성공: 50건

#### 실행 결과

```
=== Partial Success Test Result ===
Success: 50
Failure: 50
Final Balance: 0.00
```

| 항목 | 예상 | 실제 | 상태 |
|------|------|------|------|
| 최종 잔액 | 0 | 0 | ✅ |
| 성공 건수 | 50 | 50 | ✅ |
| 실패 건수 | 50 | 50 | ✅ |
| 거래 기록 | 50 | 50 | ✅ |

**검증**: 잔액 부족 시 정확히 HTTP 400 반환, 초과 출금 없음

---

## 테스트 실행 방법

### 통합 테스트 실행

```bash
# 사전 준비: Docker 컨테이너 시작
docker-compose up -d

# 전체 통합 테스트
./gradlew test --tests "*Integration*" -Dspring.profiles.active=integration

# 동시성 테스트만 실행
./gradlew test --tests "ConcurrentWithdrawalIntegrationTest"

# 비교 테스트만 실행
./gradlew test --tests "ConcurrencyControlComparisonTest"
```

**상세 테스트 결과**: [TEST_RESULTS.md](./TEST_RESULTS.md) 참조