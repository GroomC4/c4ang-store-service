package com.groom.store.common.annotation

import com.groom.platform.testcontainers.initializer.TestContainerContextInitializer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

/**
 * Store Service 통합 테스트용 어노테이션
 *
 * Testcontainers (PostgreSQL, Redis, Kafka, Schema Registry)는 자동으로 시작됩니다.
 *
 * 사용 예시:
 * ```kotlin
 * @IntegrationTest
 * @AutoConfigureMockMvc
 * class StoreControllerIntegrationTest {
 *     @Test
 *     fun `통합 테스트`() {
 *         // 테스트 로직
 *     }
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestContainerContextInitializer::class])
annotation class IntegrationTest
