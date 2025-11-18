package com.groom.store.common.base

import com.groom.store.adapter.out.client.UserServiceClient
import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc

/**
 * Store Service의 Controller 계층 통합테스트 Base Class
 *
 * Store Service에 필요한 UserServiceClient Mock과 MockMvc를 제공합니다.
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
abstract class StoreBaseControllerIntegrationTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc

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
