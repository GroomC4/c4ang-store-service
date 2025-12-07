# Kafka 토픽 설정 누락 확인 및 수정 프롬프트

## 배경

`c4ang-store-service`에서 `store.created` Kafka 토픽이 테스트 환경 설정에 누락되어 있었습니다. 다른 도메인 서비스에도 동일한 문제가 있을 수 있으니 확인 및 수정이 필요합니다.

## 문제 패턴

1. **프로덕션 코드에서 발행하는 Kafka 이벤트 토픽**이 테스트 설정에 누락
2. **토픽 설정 위치 불일치**: `@SpringBootTest(properties=[...])`와 `application-test.yml` 간 토픽 목록이 다름

## 확인 및 수정 요청 프롬프트

```
이 프로젝트의 Kafka 토픽 설정을 확인하고 누락된 토픽을 추가해주세요.

## 확인 절차

1. **프로덕션 코드에서 사용하는 Kafka 토픽 목록 확인**
   - `KafkaTopicProperties` 또는 유사한 설정 클래스에서 정의된 토픽
   - `KafkaTemplate.send()` 호출 시 사용되는 토픽명
   - `PublishEventPort` 구현체에서 발행하는 이벤트 토픽

2. **테스트 설정에서 정의된 토픽 목록 확인**
   - `IntegrationTestBase.kt`의 `@SpringBootTest(properties=[...])`
   - `ContractTestBase.kt`의 `@SpringBootTest(properties=[...])`
   - `application-test.yml`의 `testcontainers.kafka.topics` 및 `kafka.topics`

3. **누락된 토픽 식별**
   - 프로덕션에서 사용하지만 테스트 설정에 없는 토픽 찾기

## 수정 방법

누락된 토픽이 있다면 다음 3개 파일에 추가:

1. `IntegrationTestBase.kt`:
```kotlin
// Kafka Topics
"testcontainers.kafka.topics[N].name=<토픽명>",
"testcontainers.kafka.topics[N].partitions=1",
"testcontainers.kafka.topics[N].replication-factor=1",
```

2. `ContractTestBase.kt`: 동일하게 추가

3. `application-test.yml`:
```yaml
kafka:
  topics:
    <토픽-키>: <토픽명>

testcontainers:
  kafka:
    topics:
      - name: <토픽명>
        partitions: 1
        replication-factor: 1
```

## 참고 사례 (c4ang-store-service)

store-service에서 누락되었던 토픽:
- `store.created` - 스토어 생성 이벤트

이미 있던 토픽:
- `store.info.updated` - 스토어 정보 수정 이벤트
- `store.deleted` - 스토어 삭제 이벤트

## 테스트 검증

수정 후 테스트 실행하여 Kafka 관련 오류가 없는지 확인:
```bash
./gradlew test -x integrationTest
```

`Topic X not present in metadata` 오류가 발생하면 해당 토픽이 누락된 것입니다.
```

---

## 대상 프로젝트

아래 도메인 서비스들에 대해 위 프롬프트를 실행해주세요:

- [ ] c4ang-order-service
- [ ] c4ang-product-service
- [ ] c4ang-payment-service
- [ ] c4ang-customer-service
- [ ] (기타 Kafka를 사용하는 서비스)
