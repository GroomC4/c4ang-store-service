# Contract 관리 가이드

## 목차
1. [개요](#개요)
2. [Contract 관리 전략](#contract-관리-전략)
3. [HTTP API Contract (Spring Cloud Contract)](#http-api-contract-spring-cloud-contract)
4. [Event Contract (Avro Schema)](#event-contract-avro-schema)
5. [c4ang-contract-hub 작업 가이드](#c4ang-contract-hub-작업-가이드)
6. [체크리스트](#체크리스트)

## 개요

우리 시스템은 **분산형 Contract 관리 전략**을 채택합니다.

### 핵심 원칙
> **"각 서비스에서 Contract Test를 수행"** (c4ang-contract-hub 철학)

- ✅ **서비스 자율성**: 각 서비스가 자신의 Contract를 관리
- ✅ **버전 동기화**: 서비스 코드와 Contract가 같은 저장소에 위치
- ✅ **중앙 스키마**: 이벤트 스키마만 c4ang-contract-hub에서 중앙 관리

## Contract 관리 전략

| 계약 유형 | 관리 방식 | 위치 | 도구 | 목적 |
|---------|---------|------|------|------|
| **HTTP API (Provider)** | 분산형 | 각 서비스 저장소 | Spring Cloud Contract | API 명세 검증 및 Stub 제공 |
| **Event Schema (Provider)** | 중앙형 | c4ang-contract-hub | Avro Schema | 이벤트 스키마 중앙 관리 |
| **HTTP API (Consumer)** | 분산형 | 각 서비스 저장소 | Stub Runner | 의존 API 계약 검증 |

### 왜 분산형인가?

**장점:**
- 서비스와 Contract의 버전이 항상 일치
- 각 팀이 자신의 API Contract에 대한 책임 보유
- 서비스 빌드 시 즉시 Contract 검증
- API 변경 시 즉각적인 피드백

**단점:**
- Contract 파일이 여러 저장소에 분산
- 중앙에서 모든 Contract를 한눈에 보기 어려움

→ **하지만** 서비스 자율성과 버전 일치가 더 중요하므로 분산형 채택

## HTTP API Contract (Spring Cloud Contract)

### Provider 측 (store-service)

#### 1. Contract 파일 작성

**위치:** `store-api/src/test/resources/contracts/store-api/`

**예시:** `should_get_store_by_id.yml`
```yaml
description: Store API - 유효한 스토어 ID로 조회
name: should_get_store_by_id
request:
  method: GET
  url: /api/v1/stores/750e8400-e29b-41d4-a716-446655440101
  headers:
    Content-Type: application/json
response:
  status: 200
  headers:
    Content-Type: application/json
  body:
    storeId: "750e8400-e29b-41d4-a716-446655440101"
    ownerUserId: "750e8400-e29b-41d4-a716-446655440001"
    name: "Contract Test Store"
    status: "REGISTERED"
  matchers:
    body:
      - path: $.storeId
        type: by_regex
        value: "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
```

#### 2. ContractTestBase 작성

```kotlin
@AutoConfigureMockMvc
@SqlGroup(
    Sql(scripts = ["/sql/contract-test-data.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/cleanup.sql"],
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
abstract class ContractTestBase : IntegrationTestBase() {
    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @BeforeEach
    fun setup() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext)
    }
}
```

#### 3. build.gradle.kts 설정

```kotlin
plugins {
    id("org.springframework.cloud.contract") version "4.1.4"
    `maven-publish`
}

contracts {
    testMode.set(TestMode.MOCKMVC)
    baseClassForTests.set("com.groom.store.common.ContractTestBase")
    contractsDslDir.set(file("src/test/resources/contracts"))
}

publishing {
    publications {
        create<MavenPublication>("stubs") {
            groupId = "com.groom"
            artifactId = "store-service-contract-stubs"
            version = project.version.toString()
            artifact(tasks.named("verifierStubsJar"))
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/GroomC4/c4ang-store-service")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

#### 4. CI Workflow 설정

```yaml
- name: Run tests
  run: ./gradlew test contractTest --no-daemon

publish-contract-stubs:
  name: Publish Contract Stubs
  needs: continuous-integration
  if: success() && startsWith(github.ref, 'refs/tags/v')
  steps:
    - name: Publish Contract Stubs to GitHub Packages
      run: ./gradlew :store-api:publish --no-daemon
```

### Consumer 측 (다른 서비스)

#### 1. 의존성 추가

```kotlin
testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")
```

#### 2. Consumer Contract Test 작성

```kotlin
@SpringBootTest
@AutoConfigureStubRunner(
    ids = ["com.groom:store-service-contract-stubs:+:stubs:8080"],
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class StoreApiConsumerContractTest {
    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Test
    fun `should get store by id successfully`() {
        val storeId = "750e8400-e29b-41d4-a716-446655440101"

        val response = restTemplate.getForEntity(
            "http://localhost:8080/api/v1/stores/$storeId",
            StoreResponse::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.storeId).isEqualTo(storeId)
    }
}
```

#### 3. Stub 가져오기

```bash
# Provider가 발행한 Stub을 로컬에 다운로드
./gradlew :store-api:publishToMavenLocal
```

## Event Contract (Avro Schema)

### c4ang-contract-hub에서 관리

Event Schema는 **c4ang-contract-hub 저장소**에서 중앙 관리합니다.

#### 저장소 구조
```
c4ang-contract-hub/
├── avro-schemas/
│   └── store/
│       ├── StoreCreatedEvent.avsc
│       ├── StoreInfoUpdatedEvent.avsc
│       └── StoreDeletedEvent.avsc
└── event-flows/
    └── store-management/
        └── update-store-info.md
```

### Provider 측 (store-service)

#### 1. 의존성 추가

```kotlin
// Avro 스키마
implementation("com.github.GroomC4:c4ang-contract-hub:1.0.0")
implementation("io.confluent:kafka-avro-serializer:7.5.1")
```

#### 2. 이벤트 발행

```kotlin
val event = StoreInfoUpdatedEvent.newBuilder()
    .setStoreId(store.id.toString())
    .setStoreName(store.name)
    .setStoreStatus(store.status.name)
    .setUpdatedAt(Instant.now())
    .build()

kafkaTemplate.send("store.info.updated", event)
```

### Consumer 측 (다른 서비스)

#### 1. 의존성 추가

```kotlin
implementation("com.github.GroomC4:c4ang-contract-hub:1.0.0")
implementation("io.confluent:kafka-avro-serializer:7.5.1")
```

#### 2. 이벤트 소비

```kotlin
@KafkaListener(topics = ["store.info.updated"])
fun handleStoreInfoUpdated(event: StoreInfoUpdatedEvent) {
    // 이벤트 처리
    productService.updateStoreInfo(
        storeId = UUID.fromString(event.storeId),
        storeName = event.storeName
    )
}
```

## c4ang-contract-hub 작업 가이드

### 언제 c4ang-contract-hub에 작업하는가?

✅ **작업이 필요한 경우:**
1. **새로운 이벤트 추가** (예: `StoreClosedEvent`)
2. **기존 이벤트 스키마 변경** (필드 추가/삭제)
3. **이벤트 플로우 문서화** (새로운 비즈니스 플로우)
4. **스키마 버전 업그레이드**

❌ **작업이 불필요한 경우:**
1. **HTTP API 추가/변경** → 각 서비스에서 Contract 작성
2. **HTTP API Contract Test** → 각 서비스에서 수행
3. **서비스 로직 변경** → 이벤트 스키마 불변

### 작업 프로세스

#### 1. 새 이벤트 추가 (예: StoreClosedEvent)

**Step 1: Avro 스키마 작성**

`c4ang-contract-hub/avro-schemas/store/StoreClosedEvent.avsc`
```json
{
  "type": "record",
  "name": "StoreClosedEvent",
  "namespace": "com.groom.contract.store",
  "fields": [
    {"name": "eventId", "type": "string"},
    {"name": "storeId", "type": "string"},
    {"name": "storeName", "type": "string"},
    {"name": "closedReason", "type": "string"},
    {"name": "closedAt", "type": {"type": "long", "logicalType": "timestamp-millis"}}
  ]
}
```

**Step 2: 이벤트 플로우 문서화**

`c4ang-contract-hub/event-flows/store-management/close-store.md`
```markdown
# Store Closed Event Flow

## Event Published
- Event Name: `store.closed`
- Topic: `store.closed`
- Partition Key: `storeId`

## Flow Overview
1. Store owner closes store via `DELETE /stores/{storeId}`
2. Store Service marks store as CLOSED, responds immediately
3. Publishes `store.closed` event to Kafka
4. Product Service consumes event, hides all products
5. Order Service consumes event, cancels pending orders

## Schema
See: `avro-schemas/store/StoreClosedEvent.avsc`
```

**Step 3: 버전 업데이트 및 배포**

```bash
# c4ang-contract-hub 저장소에서
git add .
git commit -m "feat: Add StoreClosedEvent schema"
git tag v1.1.0
git push origin main --tags
```

**Step 4: 서비스에서 사용**

```kotlin
// store-service/build.gradle.kts
val contractHubVersion = "1.1.0"  // 버전 업데이트
implementation("com.github.GroomC4:c4ang-contract-hub:$contractHubVersion")

// 이벤트 발행
val event = StoreClosedEvent.newBuilder()
    .setStoreId(storeId)
    .setClosedReason("Owner request")
    .build()
kafkaTemplate.send("store.closed", event)
```

#### 2. 기존 스키마 변경 (Breaking vs Non-Breaking)

**Non-Breaking Change (안전):**
- 필드 추가 (default 값 포함)
- Optional 필드로 변경

```json
{
  "fields": [
    {"name": "storeId", "type": "string"},
    {"name": "storeName", "type": "string"},
    {"name": "closedReason", "type": "string", "default": "UNKNOWN"}  // 추가
  ]
}
```

**Breaking Change (위험):**
- 필드 삭제
- 필드 타입 변경
- 필수 필드로 변경

→ **Breaking Change 시 메이저 버전 업** (v1.x.x → v2.0.0)

#### 3. Schema Registry 호환성 검증

```bash
# Schema Registry 호환성 체크
curl -X POST \
  http://localhost:8081/compatibility/subjects/store.closed-value/versions/latest \
  -H 'Content-Type: application/vnd.schemaregistry.v1+json' \
  -d '{
    "schema": "{\"type\":\"record\", ...}"
  }'
```

### c4ang-contract-hub 저장소 구조

```
c4ang-contract-hub/
├── README.md                          # 전체 가이드
├── avro-schemas/                      # Avro 스키마 정의
│   ├── store/
│   │   ├── StoreCreatedEvent.avsc
│   │   ├── StoreInfoUpdatedEvent.avsc
│   │   └── StoreDeletedEvent.avsc
│   ├── customer/
│   │   └── UserCreatedEvent.avsc
│   └── order/
│       └── OrderPlacedEvent.avsc
├── event-flows/                       # 이벤트 플로우 문서
│   ├── store-management/
│   │   ├── update-store-info.md
│   │   └── close-store.md
│   └── order-processing/
│       └── place-order.md
└── build.gradle.kts                   # Avro 코드 생성 설정
```

## 체크리스트

### HTTP API 추가/변경 시

- [ ] Contract 파일 작성 (`src/test/resources/contracts/`)
- [ ] ContractTestBase에 테스트 데이터 추가
- [ ] `./gradlew contractTest` 실행하여 검증
- [ ] README 업데이트 (API 목록)
- [ ] 태그 푸시 시 Contract Stub 자동 발행 확인

### 이벤트 추가/변경 시

- [ ] c4ang-contract-hub에 Avro 스키마 작성
- [ ] 이벤트 플로우 문서 작성
- [ ] Schema Registry 호환성 검증
- [ ] c4ang-contract-hub 버전 태그 푸시
- [ ] 서비스에서 contract-hub 버전 업데이트
- [ ] Kafka Producer/Consumer 코드 작성
- [ ] 통합 테스트 작성 및 검증

### Consumer Contract Test 추가 시

- [ ] Stub Runner 의존성 추가
- [ ] Consumer Contract Test 작성
- [ ] Provider Stub을 mavenLocal에 발행하여 테스트
- [ ] CI에서 자동 실행 확인

## 참고 자료

- [c4ang-contract-hub README](https://github.com/GroomC4/c4ang-contract-hub)
- [Spring Cloud Contract 공식 문서](https://spring.io/projects/spring-cloud-contract)
- [Avro Schema 문서](https://avro.apache.org/docs/current/)
- [Consumer-Driven Contracts 패턴](https://martinfowler.com/articles/consumerDrivenContracts.html)
- [Confluent Schema Registry](https://docs.confluent.io/platform/current/schema-registry/index.html)

## 문의

Contract 관리와 관련된 질문이나 제안사항은 c4ang-contract-hub 저장소 Issues에 등록해주세요.
