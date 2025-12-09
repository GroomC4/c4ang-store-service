package com.groom.store.adapter.inbound.web

import com.groom.store.common.base.StoreBaseControllerIntegrationTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@AutoConfigureMockMvc
@DisplayName("스토어 수정 API 테스트")
@SqlGroup(
    Sql(scripts = ["/sql/cleanup-store-controller-update.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/init-store-controller-update.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/cleanup-store-controller-update.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
class StoreControllerUpdateTests : StoreBaseControllerIntegrationTest() {
    companion object {
        private val UPDATE_OWNER_USER_ID_1 = UUID.fromString("11111111-2222-3333-4444-555555555551")
        private val UPDATE_OWNER_USER_ID_3 = UUID.fromString("33333333-2222-3333-4444-555555555553")
    }

    @Test
    @DisplayName("PATCH /api/v1/stores/{storeId} - 인증된 Owner가 자신의 스토어를 수정하면 200 OK와 수정된 스토어 정보를 반환한다")
    fun testSuccessfulStoreUpdate() {
        // given: SQL에서 생성한 User와 Store 사용
        val storeId = "11111111-2222-3333-4444-555555555571"

        // 스토어 수정 요청
        val updateRequest =
            """
            {
                "name": "수정된 스토어",
                "description": "수정된 설명"
            }
            """.trimIndent()

        // when & then: X-User-Id 헤더를 통해 인증된 Owner로 스토어 수정
        mockMvc
            .perform(
                patch("/api/v1/stores/$storeId")
                    .header("X-User-Id", UPDATE_OWNER_USER_ID_1.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequest),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.storeId").value(storeId))
            .andExpect(jsonPath("$.name").value("수정된 스토어"))
            .andExpect(jsonPath("$.description").value("수정된 설명"))
            .andExpect(jsonPath("$.status").value("REGISTERED"))
            .andExpect(jsonPath("$.updatedAt").exists())
    }

    @Test
    @DisplayName("PATCH /api/v1/stores/{storeId} - 다른 Owner의 스토어 수정 시도 시 403 Forbidden을 반환한다")
    fun testUpdateOtherOwnerStore() {
        // given: SQL에서 생성한 User 3으로, User 1의 Store 수정 시도
        val user1StoreId = "11111111-2222-3333-4444-555555555571"

        // User 1의 스토어 수정 시도
        val updateRequest =
            """
            {
                "name": "타인의 스토어 수정 시도",
                "description": "접근 권한 없음"
            }
            """.trimIndent()

        // when & then: User 3으로 User 1의 스토어 수정 시도
        mockMvc
            .perform(
                patch("/api/v1/stores/$user1StoreId")
                    .header("X-User-Id", UPDATE_OWNER_USER_ID_3.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequest),
            ).andDo(print())
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("STORE_ACCESS_DENIED"))
    }

    @Test
    @DisplayName("PATCH /api/v1/stores/{storeId} - 인증되지 않은 요청은 500 Internal Server Error를 반환한다 (X-User-Id 헤더 누락)")
    fun testUnauthorizedStoreUpdate() {
        // given
        val updateRequest =
            """
            {
                "name": "무단 수정",
                "description": "인증 없이 수정 시도"
            }
            """.trimIndent()

        val fakeStoreId =
            UUID
                .randomUUID()
                .toString()

        // when & then: X-User-Id 헤더가 없으면 IstioHeaderExtractor에서 예외 발생
        mockMvc
            .perform(
                patch("/api/v1/stores/$fakeStoreId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequest),
            ).andExpect(status().isInternalServerError)
    }

    @Test
    @DisplayName("PATCH /api/v1/stores/{storeId} - 존재하지 않는 스토어 수정 시도 시 404 Not Found를 반환한다")
    fun testUpdateNonExistentStore() {
        // given: 존재하지 않는 스토어 ID
        val nonExistentStoreId =
            UUID
                .randomUUID()
                .toString()

        // 수정 시도
        val updateRequest =
            """
            {
                "name": "없는 스토어",
                "description": "수정 불가"
            }
            """.trimIndent()

        // when & then: 인증된 Owner이지만 존재하지 않는 스토어 수정 시도
        mockMvc
            .perform(
                patch("/api/v1/stores/$nonExistentStoreId")
                    .header("X-User-Id", UPDATE_OWNER_USER_ID_1.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequest),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("STORE_NOT_FOUND"))
    }
}
