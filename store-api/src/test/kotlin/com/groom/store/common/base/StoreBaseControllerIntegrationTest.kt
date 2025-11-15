package com.groom.store.common.base

import com.groom.platform.testSupport.BaseControllerIntegrationTest
import com.groom.store.common.extension.SharedContainerExtension
import com.groom.store.outbound.client.UserServiceClient
import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Store Service의 Controller 계층 통합테스트 Base Class
 *
 * Platform-core의 BaseControllerIntegrationTest를 상속받아
 * Store Service에 필요한 UserServiceClient Mock과 SharedContainerExtension을 추가합니다.
 *
 * 사용 방법:
 * ```kotlin
 * @SqlGroup(...)
 * @DisplayName("스토어 API 테스트")
 * class StoreControllerIntegrationTest : StoreBaseControllerIntegrationTest() {
 *     @Test
 *     fun test() {
 *         every { userServiceClient.get(any()) } returns UserResponse(...)
 *
 *         mockMvc.perform(post("/api/v1/stores")
 *             .header("X-User-Id", userId)
 *             .contentType(MediaType.APPLICATION_JSON)
 *             .content(requestBody))
 *             .andExpect(status().isCreated)
 *     }
 * }
 * ```
 */
@ExtendWith(SharedContainerExtension::class)
abstract class StoreBaseControllerIntegrationTest : BaseControllerIntegrationTest() {

    /**
     * UserServiceClient Mock
     *
     * Store Service의 모든 Controller 통합테스트에서 공유됩니다.
     * 각 테스트에서 every { userServiceClient.get(...) }로 동작을 정의하세요.
     */
    @MockkBean
    protected lateinit var userServiceClient: UserServiceClient

    /**
     * 각 테스트 실행 전 Mock 초기화
     *
     * 테스트 간 격리를 보장하기 위해 모든 Mock을 초기화합니다.
     */
    @BeforeEach
    fun setupMocks() {
        clearAllMocks()
    }
}
