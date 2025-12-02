package com.groom.store.common

import com.groom.platform.testcontainers.annotation.IntegrationTest
import io.restassured.module.mockmvc.RestAssuredMockMvc
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.web.context.WebApplicationContext

/**
 * Internal API Contract Test를 위한 Base 클래스
 *
 * 서비스 간 통신(Internal API)에 사용되는 Contract를 검증합니다.
 * - /internal/v1/** 엔드포인트 대상
 * - 인증 없이 호출 (API Gateway/Istio에서 이미 인증 완료)
 * - Consumer(product-service 등)가 정의한 Contract를 Provider(store-service)가 검증
 */
@IntegrationTest
@SpringBootTest(
    properties = [
        "spring.profiles.active=test",
        "testcontainers.postgres.enabled=true",
        "testcontainers.postgres.replica-enabled=true",
        "testcontainers.postgres.schema-location=project:sql/schema.sql",
        "testcontainers.redis.enabled=true",
        "testcontainers.kafka.enabled=true",
        "testcontainers.kafka.auto-create-topics=true",
        "testcontainers.kafka.topics[0].name=store.info.updated",
        "testcontainers.kafka.topics[0].partitions=3",
        "testcontainers.kafka.topics[0].replication-factor=1",
        "testcontainers.kafka.topics[1].name=store.deleted",
        "testcontainers.kafka.topics[1].partitions=1",
        "testcontainers.kafka.topics[1].replication-factor=1",
    ],
)
@AutoConfigureMockMvc
@SqlGroup(
    Sql(scripts = ["/sql/contract-test-data.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/cleanup.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
abstract class InternalContractTestBase {
    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @BeforeEach
    fun setup() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext)
    }
}
