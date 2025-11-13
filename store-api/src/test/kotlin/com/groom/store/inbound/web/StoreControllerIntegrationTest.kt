package com.groom.store.inbound.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.groom.ecommerce.store.presentation.web.dto.RegisterStoreRequest
import com.groom.store.common.TransactionApplier
import com.groom.store.common.annotation.IntegrationTest
import com.groom.store.outbound.repository.StoreRepositoryImpl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
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
@DisplayName("스토어 컨트롤러 통합 테스트")
class StoreControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var registerOwnerService: RegisterOwnerService

    @Autowired
    private lateinit var registerCustomerService: RegisterCustomerService

    @Autowired
    private lateinit var userRepository: UserRepositoryImpl

    @Autowired
    private lateinit var storeRepository: StoreRepositoryImpl

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepositoryImpl

    @Autowired
    private lateinit var transactionApplier: TransactionApplier

    private val createdEmails = mutableListOf<String>()

    @BeforeEach
    fun setUp() {
        createdEmails.clear()
    }

    @AfterEach
    fun tearDown() {
        transactionApplier.applyPrimaryTransaction {
            createdEmails.forEach { email ->
                userRepository.findByEmail(email).ifPresent { user ->
                    refreshTokenRepository.findByUserId(user.id!!).ifPresent { token ->
                        refreshTokenRepository.delete(token)
                    }
                    storeRepository.findByOwnerUserId(user.id!!).ifPresent { store ->
                        storeRepository.delete(store)
                    }
                    userRepository.delete(user)
                }
            }
        }
    }

    private fun trackEmail(email: String) {
        createdEmails.add(email)
    }

    @Nested
    @DisplayName("스토어 등록 API 테스트")
    inner class RegisterStoreTests {
        @Test
        @DisplayName("POST /api/v1/stores - 인증된 Owner가 스토어를 등록하면 201 Created와 스토어 정보를 반환한다")
        fun testSuccessfulStoreRegistration() {
            // given: Owner 회원가입 (스토어 없이)
            val registerCommand =
                RegisterOwnerCommand(
                    username = "스토어주인",
                    email = "storeowner@example.com",
                    rawPassword = "password123!",
                    phoneNumber = "010-1111-2222",
                )
            trackEmail(registerCommand.email)

            val registeredOwner = registerOwnerService.register(registerCommand)

            // 기존 스토어 삭제 (새로 등록할 수 있도록)
            transactionApplier.applyPrimaryTransaction {
                storeRepository
                    .findByOwnerUserId(
                        UUID.fromString(registeredOwner.userId),
                    ).ifPresent { store ->
                        storeRepository.delete(store)
                    }
            }

            // 로그인하여 Access Token 획득
            val loginRequest =
                LoginRequest(
                    email = "storeowner@example.com",
                    password = "password123!",
                )

            val loginResponse =
                mockMvc
                    .perform(
                        post("/api/v1/auth/owners/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)),
                    ).andExpect(status().isOk)
                    .andReturn()
                    .response
                    .contentAsString

            val loginResponseBody = objectMapper.readTree(loginResponse)
            val accessToken = loginResponseBody.get("accessToken").asText()

            // 스토어 등록 요청
            val registerStoreRequest =
                RegisterStoreRequest(
                    name = "새로운 테크 스토어",
                    description = "최신 전자제품 판매점",
                )

            // when & then
            mockMvc
                .perform(
                    post("/api/v1/stores")
                        .header("Authorization", "Bearer $accessToken")
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
        @DisplayName("POST /api/v1/stores - 인증되지 않은 요청은 401 Unauthorized를 반환한다")
        fun testUnauthorizedStoreRegistration() {
            // given
            val registerStoreRequest =
                RegisterStoreRequest(
                    name = "무단 스토어",
                    description = "인증 없이 등록 시도",
                )

            // when & then
            mockMvc
                .perform(
                    post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerStoreRequest)),
                ).andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("POST /api/v1/stores - CUSTOMER 역할을 가진 사용자가 스토어 등록 시도 시 403 Forbidden을 반환한다")
        fun testCustomerRoleStoreRegistration() {
            // given: Customer 회원가입
            val registerCommand =
                RegisterCustomerCommand(
                    username = "일반고객",
                    email = "customer@example.com",
                    rawPassword = "password123!",
                    defaultAddress = "서울시 강남구",
                    defaultPhoneNumber = "010-3333-4444",
                )
            trackEmail(registerCommand.email)

            transactionApplier.applyPrimaryTransaction {
                registerCustomerService.register(registerCommand)
            }

            // 로그인하여 Access Token 획득
            val loginRequest =
                LoginRequest(
                    email = "customer@example.com",
                    password = "password123!",
                )

            val loginResponse =
                mockMvc
                    .perform(
                        post("/api/v1/auth/customers/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)),
                    ).andExpect(status().isOk)
                    .andReturn()
                    .response
                    .contentAsString

            val loginResponseBody = objectMapper.readTree(loginResponse)
            val accessToken = loginResponseBody.get("accessToken").asText()

            // 스토어 등록 시도
            val registerStoreRequest =
                RegisterStoreRequest(
                    name = "고객의 스토어",
                    description = "CUSTOMER는 스토어를 등록할 수 없음",
                )

            // when & then
            mockMvc
                .perform(
                    post("/api/v1/stores")
                        .header("Authorization", "Bearer $accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerStoreRequest)),
                ).andDo(print()) // 응답 데이터 로그 출력
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("이 작업을 수행할 권한이 없습니다."))
        }
    }

    @Nested
    @DisplayName("스토어 수정 API 테스트")
    @SqlGroup(
        Sql(scripts = ["/sql/store/cleanup-store-controller-update.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        Sql(scripts = ["/sql/store/init-store-controller-update.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        Sql(scripts = ["/sql/store/cleanup-store-controller-update.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
    )
    inner class UpdateStoreTests {
        @Test
        @DisplayName("PATCH /api/v1/stores/{storeId} - 인증된 Owner가 자신의 스토어를 수정하면 200 OK와 수정된 스토어 정보를 반환한다")
        fun testSuccessfulStoreUpdate() {
            // given: SQL에서 생성한 User와 Store 사용
            val storeId = "11111111-2222-3333-4444-555555555571"

            // 로그인하여 Access Token 획득 (password는 SQL에서 더미 해시를 사용했으므로 실제 비밀번호는 테스트용)
            val loginRequest =
                LoginRequest(
                    email = "updateowner1@test.com",
                    password = "password123!",
                )

            val loginResponse =
                mockMvc
                    .perform(
                        post("/api/v1/auth/owners/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)),
                    ).andExpect(status().isOk)
                    .andReturn()
                    .response
                    .contentAsString

            val loginResponseBody = objectMapper.readTree(loginResponse)
            val accessToken = loginResponseBody.get("accessToken").asText()

            // 스토어 수정 요청
            val updateRequest =
                """
                {
                    "name": "수정된 스토어",
                    "description": "수정된 설명"
                }
                """.trimIndent()

            // when & then
            mockMvc
                .perform(
                    patch("/api/v1/stores/$storeId")
                        .header("Authorization", "Bearer $accessToken")
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
            // given: SQL에서 생성한 User 3으로 로그인, User 1의 Store 수정 시도
            val user1StoreId = "11111111-2222-3333-4444-555555555571"

            // User 3으로 로그인
            val loginRequest =
                LoginRequest(
                    email = "updateowner3@test.com",
                    password = "password123!",
                )

            val loginResponse =
                mockMvc
                    .perform(
                        post("/api/v1/auth/owners/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)),
                    ).andExpect(status().isOk)
                    .andReturn()
                    .response
                    .contentAsString

            val loginResponseBody = objectMapper.readTree(loginResponse)
            val accessToken = loginResponseBody.get("accessToken").asText()

            // User 1의 스토어 수정 시도
            val updateRequest =
                """
                {
                    "name": "타인의 스토어 수정 시도",
                    "description": "접근 권한 없음"
                }
                """.trimIndent()

            // when & then
            mockMvc
                .perform(
                    patch("/api/v1/stores/$user1StoreId")
                        .header("Authorization", "Bearer $accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest),
                ).andDo(print())
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.code").value("STORE_ACCESS_DENIED"))
        }

        @Test
        @DisplayName("PATCH /api/v1/stores/{storeId} - 인증되지 않은 요청은 401 Unauthorized를 반환한다")
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

            // when & then
            mockMvc
                .perform(
                    patch("/api/v1/stores/$fakeStoreId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest),
                ).andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("PATCH /api/v1/stores/{storeId} - 존재하지 않는 스토어 수정 시도 시 404 Not Found를 반환한다")
        fun testUpdateNonExistentStore() {
            // given: Owner 회원가입
            val registerCommand =
                RegisterOwnerCommand(
                    username = "주인",
                    email = "notfound@example.com",
                    rawPassword = "password123!",
                    phoneNumber = "010-3333-3333",
                )
            trackEmail(registerCommand.email)

            transactionApplier.applyPrimaryTransaction {
                registerOwnerService.register(registerCommand)
            }

            // 로그인
            val loginRequest =
                LoginRequest(
                    email = "notfound@example.com",
                    password = "password123!",
                )

            val loginResponse =
                mockMvc
                    .perform(
                        post("/api/v1/auth/owners/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)),
                    ).andExpect(status().isOk)
                    .andReturn()
                    .response
                    .contentAsString

            val loginResponseBody = objectMapper.readTree(loginResponse)
            val accessToken = loginResponseBody.get("accessToken").asText()

            // 존재하지 않는 스토어 ID
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

            // when & then
            mockMvc
                .perform(
                    patch("/api/v1/stores/$nonExistentStoreId")
                        .header("Authorization", "Bearer $accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest),
                ).andExpect(status().isNotFound)
                .andExpect(jsonPath("$.code").value("STORE_NOT_FOUND"))
        }
    }
}
