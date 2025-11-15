package com.groom.store.inbound.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.groom.store.adapter.inbound.web.dto.RegisterStoreRequest
import com.groom.store.adapter.out.persistence.StoreRepository
import com.groom.store.common.TransactionApplier
import com.groom.store.common.annotation.IntegrationTest
import com.groom.store.common.config.MockUserServiceConfig
import com.groom.store.common.util.IstioHeaderExtractor
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@IntegrationTest
@SpringBootTest
@AutoConfigureMockMvc
@Import(MockUserServiceConfig::class)
@DisplayName("스토어 컨트롤러 통합 테스트")
class StoreControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var storeRepository: StoreRepository

    @Autowired
    private lateinit var transactionApplier: TransactionApplier

    companion object {
        // Test user IDs from SQL scripts
        private val OWNER_USER_ID_1 = UUID.fromString("aaaaaaaa-1111-2222-3333-444444444441")
        private val CUSTOMER_USER_ID_1 = UUID.fromString("bbbbbbbb-1111-2222-3333-444444444442")
        private val UPDATE_OWNER_USER_ID_1 = UUID.fromString("11111111-2222-3333-4444-555555555551")
        private val UPDATE_OWNER_USER_ID_3 = UUID.fromString("33333333-2222-3333-4444-555555555553")
    }

    @Nested
    @DisplayName("스토어 등록 API 테스트")
    @SqlGroup(
        Sql(scripts = ["/sql/cleanup-store-controller-register.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        Sql(scripts = ["/sql/init-store-controller-register.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        Sql(scripts = ["/sql/cleanup-store-controller-register.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    )
    inner class RegisterStoreTests {
        @Test
        @DisplayName("POST /api/v1/stores - 인증된 Owner가 스토어를 등록하면 201 Created와 스토어 정보를 반환한다")
        fun testSuccessfulStoreRegistration() {
            // given: SQL에서 생성한 Owner User 사용 (OWNER_USER_ID_1)
            val registerStoreRequest =
                RegisterStoreRequest(
                    name = "새로운 테크 스토어",
                    description = "최신 전자제품 판매점",
                )

            // when & then: X-User-Id 헤더를 통해 인증된 사용자임을 시뮬레이션
            mockMvc
                .perform(
                    post("/api/v1/stores")
                        .header(IstioHeaderExtractor.USER_ID_HEADER, OWNER_USER_ID_1.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerStoreRequest)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.storeId").exists())
                .andExpect(jsonPath("$.name").value("새로운 테크 스토어"))
                .andExpect(jsonPath("$.description").value("최신 전자제품 판매점"))
                .andExpect(jsonPath("$.status").value("REGISTERED"))
                .andExpect(jsonPath("$.createdAt").exists())
        }

        @Test
        @DisplayName("POST /api/v1/stores - 인증되지 않은 요청은 500 Internal Server Error를 반환한다 (X-User-Id 헤더 누락)")
        fun testUnauthorizedStoreRegistration() {
            // given
            val registerStoreRequest =
                RegisterStoreRequest(
                    name = "무단 스토어",
                    description = "인증 없이 등록 시도",
                )

            // when & then: X-User-Id 헤더가 없으면 IstioHeaderExtractor에서 예외 발생
            mockMvc
                .perform(
                    post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerStoreRequest)),
                ).andExpect(status().isInternalServerError)
        }

        @Test
        @DisplayName("POST /api/v1/stores - CUSTOMER 역할을 가진 사용자가 스토어 등록 시도 시 403 Forbidden을 반환한다")
        fun testCustomerRoleStoreRegistration() {
            // given: SQL에서 생성한 Customer User 사용 (CUSTOMER_USER_ID_1)
            val registerStoreRequest =
                RegisterStoreRequest(
                    name = "고객의 스토어",
                    description = "CUSTOMER는 스토어를 등록할 수 없음",
                )

            // when & then: CUSTOMER 역할의 사용자로 스토어 등록 시도
            mockMvc
                .perform(
                    post("/api/v1/stores")
                        .header(IstioHeaderExtractor.USER_ID_HEADER, CUSTOMER_USER_ID_1.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerStoreRequest)),
                ).andDo(print())
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_PERMISSION"))
        }
    }

    @Nested
    @DisplayName("스토어 수정 API 테스트")
    @SqlGroup(
        Sql(scripts = ["/sql/cleanup-store-controller-update.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        Sql(scripts = ["/sql/init-store-controller-update.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        Sql(scripts = ["/sql/cleanup-store-controller-update.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    )
    inner class UpdateStoreTests {
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
                        .header(IstioHeaderExtractor.USER_ID_HEADER, UPDATE_OWNER_USER_ID_1.toString())
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
                        .header(IstioHeaderExtractor.USER_ID_HEADER, UPDATE_OWNER_USER_ID_3.toString())
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
                        .header(IstioHeaderExtractor.USER_ID_HEADER, UPDATE_OWNER_USER_ID_1.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest),
                ).andExpect(status().isNotFound)
                .andExpect(jsonPath("$.code").value("STORE_NOT_FOUND"))
        }
    }
}
