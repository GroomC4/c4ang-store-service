package com.groom.store.common.annotation

import com.groom.store.common.extension.SharedContainerExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import com.groom.platform.testSupport.IntegrationTest as BaseIntegrationTest

/**
 * Store Service 통합 테스트용 어노테이션
 *
 * c4ang-platform-core의 BaseIntegrationTest를 상속받아 Store Service에 필요한
 * 컨테이너 Extension을 추가합니다.
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
@BaseIntegrationTest
@SpringBootTest
@ExtendWith(SharedContainerExtension::class)
annotation class IntegrationTest
