# Kafka 설정 마이그레이션 TODO

## 배경
CI 테스트에서 `StoreControllerUpdateTests`가 실패함.
- 원인: `KafkaProducerConfig`가 커스텀 `kafka.bootstrap-servers` 프로퍼티를 사용하고 있었음
- Testcontainers는 `spring.kafka.bootstrap-servers`로 동적 포트를 주입함
- 두 설정이 연결되지 않아 Kafka Producer가 잘못된 주소로 연결 시도

## 진행된 작업 (WIP 커밋: 4491e40)

### 1. KafkaProducerConfig 수정
**파일**: `store-api/src/main/kotlin/com/groom/store/configuration/kafka/KafkaProducerConfig.kt`

- 커스텀 `KafkaProperties` → Spring Boot 표준 `org.springframework.boot.autoconfigure.kafka.KafkaProperties` 사용
- `buildProducerProperties()` 메서드로 Spring Boot 설정 자동 적용
- `KafkaTopicProperties` 클래스 분리 (토픽명 설정용)

### 2. StoreEventAdapter 수정
**파일**: `store-api/src/main/kotlin/com/groom/store/adapter/out/event/StoreEventAdapter.kt`

- `KafkaProperties` → `KafkaTopicProperties`로 변경
- `kafkaProperties.topics.storeInfoUpdated` → `kafkaTopicProperties.storeInfoUpdated`로 변경

### 3. application.yml 수정
**파일**: `store-api/src/main/resources/application.yml`

변경 전:
```yaml
kafka:
  bootstrap-servers: localhost:9092
  producer:
    acks: all
    ...
  topics:
    store-info-updated: store.info.updated
```

변경 후:
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      acks: all
      ...

kafka:
  topics:
    store-info-updated: store.info.updated
```

### 4. application-test.yml 수정
**파일**: `store-api/src/test/resources/application-test.yml`

- 불필요한 `kafka.bootstrap-servers` 제거 (Testcontainers가 `spring.kafka.bootstrap-servers` 자동 주입)
- `kafka.topics` 설정만 유지

## 남은 작업

### 1. CI 테스트 검증
```bash
GITHUB_ACTOR="hayden-han" GITHUB_TOKEN="..." ./gradlew :store-api:test --no-daemon
```

특히 다음 테스트 확인:
- `StoreControllerUpdateTests` - Kafka 이벤트 발행 테스트
- `UserServiceFeignClientUnitTest` - 단위 테스트
- `UserServiceFeignClientConsumerContractTest` - Contract 테스트

### 2. 실패 시 확인 사항

#### Testcontainers Kafka 동적 포트 주입 확인
`IntegrationTestBase.kt`에서 Kafka Testcontainer가 `spring.kafka.bootstrap-servers`를 제대로 주입하는지 확인:
```kotlin
"testcontainers.kafka.enabled=true"
```

platform-core의 `@IntegrationTest` 어노테이션이 동적 포트를 주입하는지 확인 필요.

#### application.yml 설정 확인
`spring.kafka.producer` 설정이 Spring Boot 표준과 맞는지 확인:
```yaml
spring:
  kafka:
    producer:
      batch-size: 16KB    # DataSize 형식
      buffer-size: 32MB   # 이 필드명이 맞는지 확인 필요
```

참고: Spring Boot `KafkaProperties.Producer`의 실제 필드명:
- `batchSize` (DataSize)
- `bufferMemory` (DataSize) - `buffer-size`가 아닐 수 있음

### 3. 로컬 테스트 실행
Docker가 실행 중인 상태에서:
```bash
./gradlew :store-api:test
```

## 참고 자료
- Spring Boot Kafka Auto-configuration: https://docs.spring.io/spring-boot/docs/current/reference/html/messaging.html#messaging.kafka
- KafkaProperties 소스: `org.springframework.boot.autoconfigure.kafka.KafkaProperties`

## PR 정보
- Branch: `feature/applying-renewal-platform-core`
- PR: https://github.com/GroomC4/c4ang-store-service/pull/3
