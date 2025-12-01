# Store Service

C4ang 이커머스 플랫폼의 **스토어 관리 마이크로서비스**입니다.

## 서비스 책임

- 스토어 등록, 조회, 수정, 삭제 (CRUD)
- 스토어 소유자 권한 검증
- 스토어 상태 관리 (REGISTERED, SUSPENDED, HIDDEN, DELETED)
- 스토어 평점 관리
- 스토어 변경 이력 감사 로그 기록
- 도메인 이벤트 발행 (Kafka)

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Kotlin 2.0, JDK 21 |
| Framework | Spring Boot 3.3 |
| Database | PostgreSQL (Primary-Replica) |
| Cache | Redis (Redisson) |
| Message Broker | Apache Kafka |
| API Documentation | SpringDoc OpenAPI |
| Build Tool | Gradle 8.x (Kotlin DSL) |
| Container | Docker |

## 프로젝트 구조

```
store-api/
└── src/main/kotlin/com/groom/store/
    ├── adapter/                    # 외부 시스템 연동
    │   ├── inbound/web/           # REST API Controller
    │   └── out/
    │       ├── persistence/       # JPA Repository
    │       ├── event/             # Kafka Producer
    │       └── client/            # Feign Client (customer-service)
    │
    ├── application/               # 애플리케이션 서비스
    │   ├── service/               # Use Case 구현
    │   ├── dto/                   # Command, Result DTO
    │   └── event/                 # 도메인 이벤트 핸들러
    │
    ├── domain/                    # 도메인 계층 (비즈니스 로직)
    │   ├── model/                 # Entity, Value Object
    │   ├── service/               # 도메인 서비스
    │   ├── port/                  # 포트 인터페이스
    │   └── event/                 # 도메인 이벤트
    │
    ├── common/                    # 공통 유틸리티
    │   ├── exception/             # 예외 처리
    │   ├── idempotency/           # 멱등성 처리
    │   └── enums/                 # 공통 enum
    │
    └── configuration/             # 설정 클래스
        ├── kafka/                 # Kafka 설정
        ├── feign/                 # Feign Client 설정
        └── jpa/                   # JPA/DataSource 설정
```

## API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/stores` | 스토어 등록 |
| GET | `/api/v1/stores/{storeId}` | 스토어 조회 |
| GET | `/api/v1/stores/mine` | 내 스토어 조회 |
| PATCH | `/api/v1/stores/{storeId}` | 스토어 수정 |
| DELETE | `/api/v1/stores/{storeId}` | 스토어 삭제 |

## 발행 이벤트 (Kafka)

| 이벤트 | Topic | 설명 |
|--------|-------|------|
| StoreCreatedEvent | - | 스토어 생성 시 (내부 처리) |
| StoreInfoUpdatedEvent | `store.info.updated` | 스토어 정보 수정 시 |
| StoreDeletedEvent | `store.deleted` | 스토어 삭제 시 |

## 외부 서비스 의존성

| 서비스 | 용도 |
|--------|------|
| customer-service | 사용자 정보 조회 (권한 검증) |

## 환경 설정

### 프로필

| 프로필 | 용도 | 설명 |
|--------|------|------|
| (default) | 로컬 개발 | H2/PostgreSQL 직접 연결 |
| test | 테스트 | Testcontainers 사용 |
| dev | k3d 개발환경 | Kubernetes 서비스 이름 사용 |
| prod | 운영환경 | 환경변수 기반 설정 |

### 주요 환경변수 (prod)

```
DB_MASTER_URL, DB_REPLICA_URL, DB_USERNAME, DB_PASSWORD
REDIS_HOST, REDIS_PORT, REDIS_PASSWORD
KAFKA_BOOTSTRAP_SERVERS
CUSTOMER_SERVICE_URL
JWT_SECRET
```

## 빌드 및 실행

```bash
# 빌드 (테스트 포함)
./gradlew build

# 빌드 (테스트 제외)
./gradlew build -x test

# 단위 테스트
./gradlew :store-api:test

# 통합 테스트 (Testcontainers)
./gradlew :store-api:integrationTest

# Contract 테스트
./gradlew :store-api:contractTest

# 로컬 실행
./gradlew bootRun
```

## Docker

```bash
# 빌드
docker build \
  --build-arg GITHUB_ACTOR=<username> \
  --build-arg GITHUB_TOKEN=<token> \
  -t store-service .

# 실행
docker run -p 8082:8082 store-service
```

## Contract 테스트

### Provider Contract (제공하는 API)

- Spring Cloud Contract를 사용하여 API 계약 검증
- Contract 파일: `store-api/src/test/resources/contracts/`
- Stub 발행: GitHub Packages (`com.groom:store-service-contract-stubs`)

### Consumer Contract (소비하는 API)

- customer-service의 사용자 조회 API 계약 검증
- Stub Runner를 통한 Consumer Contract Test

```bash
# Contract Stub 발행
./gradlew :store-api:publish
```

## 관련 저장소

- [c4ang-customer-service](https://github.com/GroomC4/c4ang-customer-service) - 고객 서비스
- [c4ang-platform-core](https://github.com/GroomC4/c4ang-platform-core) - 공통 플랫폼 라이브러리
