package com.groom.store.common.base

import com.groom.store.common.IntegrationTestBase
import com.groom.store.common.wiremock.WireMockUserServiceConfig
import com.groom.store.domain.port.PublishEventPort
import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.justRun
import org.junit.jupiter.api.BeforeEach

/**
 * Store Service의 Service 계층 통합테스트 Base Class
 *
 * Store Service에 필요한 PublishEventPort Mock을 제공합니다.
 * UserServiceFeignClient는 WireMock 서버를 통해 stub 응답을 받습니다.
 *
 * 사용 방법:
 * ```kotlin
 * @SqlGroup(...)
 * class RegisterStoreServiceIntegrationTest : StoreBaseServiceIntegrationTest() {
 *     @Test
 *     fun test() {
 *         // WireMock이 TestUserRegistry의 사용자 정보를 stub 응답으로 반환합니다.
 *         // 테스트 로직
 *     }
 * }
 * ```
 *
 * 커스텀 사용자 응답이 필요한 경우:
 * ```kotlin
 * @Test
 * fun `특정 사용자 테스트`() {
 *     WireMockUserServiceConfig.stubUser(customUserId, "Custom User", "OWNER")
 *     // 테스트 로직
 * }
 * ```
 */
abstract class StoreBaseServiceIntegrationTest : IntegrationTestBase() {
    /**
     * PublishEventPort Mock
     *
     * Kafka 이벤트 발행을 Mock합니다.
     * 기본적으로 모든 이벤트 발행이 성공한 것으로 처리됩니다.
     */
    @MockkBean
    protected lateinit var publishEventPort: PublishEventPort

    /**
     * 각 테스트 실행 전 Mock 초기화 및 설정
     *
     * 테스트 간 격리를 보장하기 위해 모든 Mock을 초기화합니다.
     * PublishEventPort는 기본 동작으로 모든 이벤트 발행을 허용합니다.
     */
    @BeforeEach
    fun setupMocks() {
        clearAllMocks()

        // PublishEventPort 기본 동작 설정 - 모든 이벤트 발행 허용
        justRun { publishEventPort.publishStoreCreated(any()) }
        justRun { publishEventPort.publishStoreInfoUpdated(any()) }
        justRun { publishEventPort.publishStoreDeleted(any()) }
    }
}
