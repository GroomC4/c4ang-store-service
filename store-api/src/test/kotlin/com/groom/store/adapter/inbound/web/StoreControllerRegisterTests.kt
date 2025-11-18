package com.groom.store.adapter.inbound.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.groom.store.adapter.inbound.web.dto.RegisterStoreRequest
import com.groom.store.adapter.out.client.UserResponse
import com.groom.store.adapter.out.client.UserRole
import com.groom.store.common.base.StoreBaseControllerIntegrationTest
import com.groom.store.common.util.IstioHeaderExtractor
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@AutoConfigureMockMvc
@DisplayName("스토어 등록 API 테스트")
@SqlGroup(
    Sql(scripts = ["/sql/cleanup-store-controller-register.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/init-store-controller-register.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/cleanup-store-controller-register.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
class StoreControllerRegisterTests : StoreBaseControllerIntegrationTest() {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        private val OWNER_USER_ID_1 = UUID.fromString("aaaaaaaa-1111-2222-3333-444444444441")
        private val CUSTOMER_USER_ID_1 = UUID.fromString("bbbbbbbb-1111-2222-3333-444444444442")
    }

    @Test
    @DisplayName("POST /api/v1/stores - 인증된 Owner가 스토어를 등록하면 201 Created와 스토어 정보를 반환한다")
    fun testSuccessfulStoreRegistration() {
        // given: SQL에서 생성한 Owner User 사용 (OWNER_USER_ID_1)
        every { userServiceClient.get(OWNER_USER_ID_1) } returns
            UserResponse(
                id = OWNER_USER_ID_1,
                name = "Owner User 1",
                role = UserRole.OWNER,
            )

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

    @org.junit.jupiter.api.Disabled(
        "Spring Cloud Contract stubs return OWNER role by default. This scenario should be tested in user-service as a producer contract test.",
    )
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
