package com.groom.store.adapter.inbound.web

import com.groom.store.adapter.out.persistence.StoreRepository
import com.groom.store.common.TransactionApplier
import com.groom.store.common.base.StoreBaseControllerIntegrationTest
import com.groom.store.common.util.IstioHeaderExtractor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@AutoConfigureMockMvc
@DisplayName("스토어 삭제 API 테스트")
@SqlGroup(
    Sql(scripts = ["/sql/cleanup-store-controller-delete.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/init-store-controller-delete.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/cleanup-store-controller-delete.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
class StoreControllerDeleteTests : StoreBaseControllerIntegrationTest() {
    @Autowired
    private lateinit var storeRepository: StoreRepository

    @Autowired
    private lateinit var transactionApplier: TransactionApplier

    companion object {
        private val UPDATE_OWNER_USER_ID_1 = UUID.fromString("11111111-2222-3333-4444-555555555551")
        private val UPDATE_OWNER_USER_ID_2 = UUID.fromString("22222222-2222-3333-4444-555555555552")
        private val UPDATE_OWNER_USER_ID_3 = UUID.fromString("33333333-2222-3333-4444-555555555553")
    }

    @Test
    @DisplayName("DELETE /api/v1/stores/{storeId} - 인증된 Owner가 자신의 스토어를 성공적으로 삭제한다")
    fun testSuccessfulStoreDeletion() {
        // given: SQL에서 생성한 Owner User와 Store 사용
        val storeId = "11111111-2222-3333-4444-555555555571"

        // when & then
        mockMvc
            .perform(
                delete("/api/v1/stores/$storeId")
                    .header(IstioHeaderExtractor.USER_ID_HEADER, UPDATE_OWNER_USER_ID_1.toString()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.storeId").value(storeId))
            .andExpect(jsonPath("$.ownerUserId").value(UPDATE_OWNER_USER_ID_1.toString()))
            .andExpect(jsonPath("$.name").value("Delete Test Store 1"))
            .andExpect(jsonPath("$.deletedAt").exists())

        // 삭제 확인: DB에서 실제로 DELETED 상태로 변경되었는지 확인
        val deletedStore =
            transactionApplier.applyPrimaryTransaction {
                storeRepository.findById(UUID.fromString(storeId)).orElse(null)
            }
        assertThat(deletedStore).isNotNull()
        assertThat(deletedStore?.status).isEqualTo(com.groom.store.common.enums.StoreStatus.DELETED)
    }

    @Test
    @DisplayName("DELETE /api/v1/stores/{storeId} - 다른 Owner의 스토어 삭제 시도 시 403 Forbidden을 반환한다")
    fun testDeleteOtherOwnerStore() {
        // given: SQL에서 생성한 User 3으로, User 1의 Store 삭제 시도
        val user1StoreId = "11111111-2222-3333-4444-555555555571"

        // when & then
        mockMvc
            .perform(
                delete("/api/v1/stores/$user1StoreId")
                    .header(IstioHeaderExtractor.USER_ID_HEADER, UPDATE_OWNER_USER_ID_3.toString()),
            ).andDo(print())
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value("STORE_ACCESS_DENIED"))
    }

    @Test
    @DisplayName("DELETE /api/v1/stores/{storeId} - 존재하지 않는 스토어 삭제 시도 시 404 Not Found를 반환한다")
    fun testDeleteNonExistentStore() {
        // given
        val nonExistentStoreId =
            UUID
                .randomUUID()
                .toString()

        // when & then
        mockMvc
            .perform(
                delete("/api/v1/stores/$nonExistentStoreId")
                    .header(IstioHeaderExtractor.USER_ID_HEADER, UPDATE_OWNER_USER_ID_1.toString()),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("STORE_NOT_FOUND"))
    }

    @Test
    @DisplayName("DELETE /api/v1/stores/{storeId} - 인증되지 않은 요청은 500 Internal Server Error를 반환한다")
    fun testDeleteStore_Unauthorized() {
        // given
        val fakeStoreId =
            UUID
                .randomUUID()
                .toString()

        // when & then: X-User-Id 헤더가 없으면 IstioHeaderExtractor에서 예외 발생
        mockMvc
            .perform(delete("/api/v1/stores/$fakeStoreId"))
            .andExpect(status().isInternalServerError)
    }

    @Test
    @DisplayName("DELETE /api/v1/stores/{storeId} - 이미 삭제된 스토어 재삭제 시도 시 409 Conflict를 반환한다")
    fun testDeleteAlreadyDeletedStore() {
        // given: 이미 삭제된 스토어 (SQL에서 DELETED 상태로 생성)
        val deletedStoreId = "22222222-2222-3333-4444-555555555572"

        // when & then
        mockMvc
            .perform(
                delete("/api/v1/stores/$deletedStoreId")
                    .header(IstioHeaderExtractor.USER_ID_HEADER, UPDATE_OWNER_USER_ID_2.toString()),
            ).andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("STORE_ALREADY_DELETED"))
    }
}
