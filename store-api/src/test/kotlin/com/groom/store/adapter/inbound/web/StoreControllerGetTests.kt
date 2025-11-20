package com.groom.store.adapter.inbound.web

import com.groom.store.common.base.StoreBaseControllerIntegrationTest
import com.groom.store.common.util.IstioHeaderExtractor
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@AutoConfigureMockMvc
@DisplayName("스토어 조회 API 테스트")
@SqlGroup(
    Sql(scripts = ["/sql/cleanup-store-controller-get.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/init-store-controller-get.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/cleanup-store-controller-get.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
class StoreControllerGetTests : StoreBaseControllerIntegrationTest() {
    companion object {
        private val UPDATE_OWNER_USER_ID_1 = UUID.fromString("11111111-2222-3333-4444-555555555551")
    }

    @Test
    @DisplayName("GET /api/v1/stores/{storeId} - 존재하는 스토어를 성공적으로 조회한다")
    fun testSuccessfulStoreRetrieval() {
        // given: SQL에서 생성한 Store 사용
        val storeId = "11111111-2222-3333-4444-555555555571"

        // when & then: 인증 없이도 스토어 조회 가능
        mockMvc
            .perform(get("/api/v1/stores/$storeId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.storeId").value(storeId))
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.createdAt").exists())
    }

    @Test
    @DisplayName("GET /api/v1/stores/{storeId} - 존재하지 않는 스토어 조회 시 404 Not Found를 반환한다")
    fun testGetNonExistentStore() {
        // given
        val nonExistentStoreId =
            UUID
                .randomUUID()
                .toString()

        // when & then
        mockMvc
            .perform(get("/api/v1/stores/$nonExistentStoreId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("STORE_NOT_FOUND"))
    }

    @Test
    @DisplayName("GET /api/v1/stores/mine - 인증된 Owner가 자신의 스토어를 조회한다")
    fun testGetMyStore() {
        // given: SQL에서 생성한 Owner User와 Store 사용
        // when & then
        mockMvc
            .perform(
                get("/api/v1/stores/mine")
                    .header(IstioHeaderExtractor.USER_ID_HEADER, UPDATE_OWNER_USER_ID_1.toString()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.storeId").exists())
            .andExpect(jsonPath("$.name").exists())
    }

    @Test
    @DisplayName("GET /api/v1/stores/mine - 스토어가 없는 Owner가 조회 시 404 Not Found를 반환한다")
    fun testGetMyStore_NoStore() {
        // given: 스토어가 없는 Owner
        val ownerWithoutStore = UUID.randomUUID()

        // when & then
        mockMvc
            .perform(
                get("/api/v1/stores/mine")
                    .header(IstioHeaderExtractor.USER_ID_HEADER, ownerWithoutStore.toString()),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("STORE_NOT_FOUND"))
    }

    @Test
    @DisplayName("GET /api/v1/stores/mine - 인증되지 않은 요청은 500 Internal Server Error를 반환한다")
    fun testGetMyStore_Unauthorized() {
        // when & then: X-User-Id 헤더가 없으면 IstioHeaderExtractor에서 예외 발생
        mockMvc
            .perform(get("/api/v1/stores/mine"))
            .andExpect(status().isInternalServerError)
    }
}
