# store-service
스토어 관리

## Contract 관리 전략

store-service는 **분산형 Contract 관리 전략**을 따릅니다. 각 서비스가 자신의 Contract를 관리하며, c4ang-contract-hub는 Avro 스키마만 중앙 관리합니다.

### 1. Provider Contract (HTTP API 제공)

store-service는 Store API를 제공하며, **Spring Cloud Contract**를 사용하여 Provider Contract Test를 수행합니다.

**제공 API:**
- `GET /api/v1/stores/{storeId}`: 스토어 조회
- `GET /api/v1/stores/mine`: 내 스토어 조회
- `POST /api/v1/stores`: 스토어 등록
- `PATCH /api/v1/stores/{storeId}`: 스토어 수정
- `DELETE /api/v1/stores/{storeId}`: 스토어 삭제

**Contract 파일 위치:**
```
store-api/src/test/resources/contracts/store-api/
├── should_get_store_by_id.yml
├── should_get_my_store.yml
├── should_register_store_successfully.yml
├── should_update_store_successfully.yml
├── should_delete_store_successfully.yml
└── should_return_404_when_store_not_found.yml
```

**Contract Test 실행:**
```bash
# Contract Test 실행 (Provider 측 검증)
./gradlew :store-api:contractTest

# Contract Stub 생성 및 로컬 발행
./gradlew :store-api:publishToMavenLocal

# Contract Stub GitHub Packages 발행 (태그 푸시 시 CI에서 자동 실행)
./gradlew :store-api:publish
```

**Consumer 사용 방법:**
다른 서비스(product-service, order-service 등)에서 Store API를 호출할 때:

```kotlin
// build.gradle.kts
testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")

// Contract Test
@AutoConfigureStubRunner(
    ids = ["com.groom:store-service-contract-stubs:+:stubs:8080"],
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class StoreApiContractTest {
    @Test
    fun `should call store api successfully`() {
        // Contract Stub을 통한 테스트
    }
}
```

### 2. Provider Contract (Kafka 이벤트 발행)

store-service는 스토어 도메인 이벤트를 Kafka를 통해 발행하며, 이벤트 스키마는 **c4ang-contract-hub**에서 Avro 형식으로 관리됩니다.

**발행 이벤트:**
- `StoreCreatedEvent`: 스토어 생성 시 발행
- `StoreInfoUpdatedEvent`: 스토어 정보 수정 시 발행
- `StoreDeletedEvent`: 스토어 삭제 시 발행

**스키마 관리:**
- Repository: [c4ang-contract-hub](https://github.com/GroomC4/c4ang-contract-hub)
- 버전 관리: Confluent Schema Registry
- 참조: [스토어 정보 업데이트 이벤트 플로우](https://github.com/GroomC4/c4ang-contract-hub/blob/main/event-flows/store-management/update-store-info.md)

### 3. Consumer Contract (HTTP API 소비)

store-service는 다른 서비스의 HTTP API를 소비하며, **Spring Cloud Contract Stub Runner**를 사용하여 계약을 검증합니다.

**소비 API:**
- `customer-service`: 사용자 정보 조회 API

**계약 테스트:**
- `UserServiceFeignClientUnitTest`: WireMock 기반 FeignClient 단위 테스트
- `UserServiceFeignClientConsumerContractTest`: Stub Runner 기반 Consumer Contract Test

```kotlin
@AutoConfigureStubRunner(
    ids = ["com.groom:customer-service-contract-stubs:+:stubs:8090"],
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
```

### 의존성
```kotlin
// Provider (HTTP API): Spring Cloud Contract
id("org.springframework.cloud.contract") version "4.1.4"
testImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier")
testImplementation("io.rest-assured:rest-assured")
testImplementation("io.rest-assured:spring-mock-mvc")

// Provider (Kafka 이벤트): Avro 스키마
implementation("com.github.GroomC4:c4ang-contract-hub:1.0.0")
implementation("io.confluent:kafka-avro-serializer:7.5.1")

// Consumer (HTTP API): Spring Cloud Contract Stub Runner
testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")
```

### Contract 철학

**핵심 원칙:** "각 서비스에서 Contract Test를 수행" (c4ang-contract-hub 참조)

- ✅ **HTTP API Contract**: 각 서비스가 자신이 제공하는 API의 Contract를 정의하고 검증
- ✅ **Event Schema**: c4ang-contract-hub에서 Avro 스키마로 중앙 관리
- ✅ **서비스 자율성**: 서비스 코드와 Contract가 같은 저장소에 있어 버전 일치 보장
