package com.groom.store.common

import com.groom.platform.testcontainers.annotation.IntegrationTest
import com.groom.store.common.wiremock.WireMockInitializer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

/**
 * 통합 테스트 베이스 클래스 (RC13 패턴)
 *
 * **사용법:**
 * 1. 통합 테스트 클래스에서 이 클래스를 상속받기
 * 2. @SpringBootTest 어노테이션 제거 (중복 방지)
 * 3. 끝! Testcontainers 자동 시작됨
 *
 * **예시:**
 * ```kotlin
 * class UpdateStoreServiceIntegrationTest : IntegrationTestBase() {
 *     @Autowired
 *     private lateinit var storeRepository: StoreRepository
 *
 *     @Test
 *     fun `테스트`() { ... }
 * }
 * ```
 *
 * **RC13 변경사항:**
 * - @IntegrationTest 어노테이션 중앙화
 * - @ContextConfiguration 제거 (@IntegrationTest에 포함됨)
 * - Kafka/Schema Registry 동적 포트 주입 자동화
 *
 * **WireMock 통합:**
 * - WireMockInitializer가 FeignClient 요청을 WireMock 서버로 라우팅
 * - TestUserRegistry에 등록된 사용자 정보가 자동으로 stub됨
 * - 커스텀 stub이 필요한 경우 WireMockUserServiceConfig 사용
 */
@IntegrationTest
@SpringBootTest(
    properties = [
        // Spring Profile
        "spring.profiles.active=test",

        // PostgreSQL
        "testcontainers.postgres.enabled=true",
        "testcontainers.postgres.replica-enabled=true",
        "testcontainers.postgres.schema-location=project:sql/schema.sql",

        // Redis
        "testcontainers.redis.enabled=true",

        // Kafka
        "testcontainers.kafka.enabled=true",
        "testcontainers.kafka.auto-create-topics=true",

        // Kafka Topics
        "testcontainers.kafka.topics[0].name=store.created",
        "testcontainers.kafka.topics[0].partitions=1",
        "testcontainers.kafka.topics[0].replication-factor=1",

        "testcontainers.kafka.topics[1].name=store.info.updated",
        "testcontainers.kafka.topics[1].partitions=3",
        "testcontainers.kafka.topics[1].replication-factor=1",

        "testcontainers.kafka.topics[2].name=store.deleted",
        "testcontainers.kafka.topics[2].partitions=1",
        "testcontainers.kafka.topics[2].replication-factor=1",
    ],
)
@ContextConfiguration(initializers = [WireMockInitializer::class])
abstract class IntegrationTestBase
