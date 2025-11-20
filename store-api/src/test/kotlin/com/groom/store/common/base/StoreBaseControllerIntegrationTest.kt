package com.groom.store.common.base

import com.groom.store.common.IntegrationTestBase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc

/**
 * Store Service의 Controller 계층 통합테스트 Base Class
 *
 * IntegrationTestBase를 상속받아 @SpringBootTest와 Testcontainers 설정을 제공받습니다.
 * Store Service에 필요한 MockMvc를 제공합니다.
 *
 * 사용 방법:
 * ```kotlin
 * @SqlGroup(...)
 * @DisplayName("스토어 API 테스트")
 * class StoreControllerIntegrationTest : StoreBaseControllerIntegrationTest() {
 *     @Test
 *     fun test() {
 *         // MockUserServiceClient가 자동으로 TestUserRegistry의 사용자 정보를 반환합니다.
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
abstract class StoreBaseControllerIntegrationTest : IntegrationTestBase() {
    @Autowired
    protected lateinit var mockMvc: MockMvc
}
