package com.groom.store.common.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.groom.store.adapter.out.client.dto.UserInternalDto
import com.groom.store.adapter.out.client.dto.UserProfileDto
import com.groom.store.fixture.TestUserRegistry
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.ContextClosedEvent
import java.util.UUID

/**
 * WireMock 서버를 통한 UserService 통합 테스트 설정
 *
 * 테스트 환경에서 실제 FeignClient가 WireMock 서버로 요청을 보내도록 설정합니다.
 * TestUserRegistry에 등록된 사용자 정보를 기반으로 stub 응답을 생성합니다.
 */
object WireMockUserServiceConfig {
    private var wireMockServer: WireMockServer? = null
    private val objectMapper = ObjectMapper()

    val port: Int
        get() = wireMockServer?.port() ?: throw IllegalStateException("WireMock server not started")

    val baseUrl: String
        get() = "http://localhost:$port"

    @Synchronized
    fun startServer(): WireMockServer {
        if (wireMockServer == null || !wireMockServer!!.isRunning) {
            wireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
            wireMockServer!!.start()
            setupDefaultStubs()
        }
        return wireMockServer!!
    }

    @Synchronized
    fun stopServer() {
        wireMockServer?.stop()
        wireMockServer = null
    }

    fun resetStubs() {
        wireMockServer?.resetAll()
        setupDefaultStubs()
    }

    /**
     * TestUserRegistry의 모든 사용자에 대한 기본 stub 설정
     */
    private fun setupDefaultStubs() {
        val server = wireMockServer ?: return

        // 모든 /internal/v1/users/{userId} 요청에 대해 동적으로 응답
        server.stubFor(
            get(urlMatching("/internal/v1/users/[a-f0-9-]+"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withTransformers("user-response-transformer"),
                ),
        )

        // Transformer가 없으면 각 사용자별로 stub 설정
        setupUserStubs(server)
    }

    /**
     * TestUserRegistry에 등록된 모든 사용자에 대한 stub 설정
     */
    private fun setupUserStubs(server: WireMockServer) {
        val testUsers = listOf(
            TestUserRegistry.OWNER_USER_1,
            TestUserRegistry.OWNER_USER_2,
            TestUserRegistry.CUSTOMER_USER_1,
            TestUserRegistry.CUSTOMER_USER_2,
            TestUserRegistry.UPDATE_OWNER_USER_1,
            TestUserRegistry.UPDATE_OWNER_USER_2,
            TestUserRegistry.UPDATE_OWNER_USER_3,
            TestUserRegistry.DELETE_OWNER_USER_1,
            TestUserRegistry.DELETE_OWNER_USER_2,
            TestUserRegistry.DELETE_OWNER_USER_3,
            TestUserRegistry.DELETE_OWNER_USER_4,
            TestUserRegistry.DELETE_OWNER_USER_5,
            TestUserRegistry.REGISTER_OWNER_USER_2,
            TestUserRegistry.REGISTER_OWNER_USER_5,
        )

        testUsers.forEach { testUser ->
            stubUserResponse(server, testUser.id, testUser.name, testUser.role.name)
        }

        // 기본 Unknown User에 대한 fallback stub (우선순위 낮음)
        server.stubFor(
            get(urlMatching("/internal/v1/users/[a-f0-9-]+"))
                .atPriority(10)
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(createDefaultUserResponse()),
                ),
        )
    }

    private fun stubUserResponse(
        server: WireMockServer,
        userId: UUID,
        userName: String,
        role: String,
    ) {
        val userDto = createUserInternalDto(userId, userName, role)
        val responseBody = objectMapper.writeValueAsString(userDto)

        server.stubFor(
            get(urlMatching("/internal/v1/users/$userId"))
                .atPriority(1)
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody),
                ),
        )
    }

    private fun createUserInternalDto(
        userId: UUID,
        userName: String,
        role: String,
    ): UserInternalDto {
        val profile = UserProfileDto(
            fullName = userName,
            phoneNumber = "+82-10-1234-5678",
            address = "서울시 강남구 테헤란로 123",
        )

        return UserInternalDto(
            userId = userId.toString(),
            username = userName.lowercase().replace(" ", "_"),
            email = "${userName.lowercase().replace(" ", "_")}@example.com",
            role = role,
            isActive = true,
            profile = profile,
            createdAt = System.currentTimeMillis() - 86400000L,
            updatedAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis(),
        )
    }

    private fun createDefaultUserResponse(): String {
        val defaultUser = UserInternalDto(
            userId = "00000000-0000-0000-0000-000000000000",
            username = "unknown_user",
            email = "unknown_user@example.com",
            role = "OWNER",
            isActive = true,
            profile = UserProfileDto(
                fullName = "Unknown User",
                phoneNumber = "+82-10-0000-0000",
                address = null,
            ),
            createdAt = System.currentTimeMillis() - 86400000L,
            updatedAt = System.currentTimeMillis(),
            lastLoginAt = null,
        )
        return objectMapper.writeValueAsString(defaultUser)
    }

    /**
     * 특정 사용자 ID에 대한 커스텀 stub 추가
     */
    fun stubUser(
        userId: UUID,
        userName: String,
        role: String,
    ) {
        val server = wireMockServer ?: throw IllegalStateException("WireMock server not started")
        stubUserResponse(server, userId, userName, role)
    }

    /**
     * 특정 사용자 ID에 대해 에러 응답 stub
     */
    fun stubUserNotFound(userId: UUID) {
        val server = wireMockServer ?: throw IllegalStateException("WireMock server not started")
        server.stubFor(
            get(urlMatching("/internal/v1/users/$userId"))
                .atPriority(1)
                .willReturn(
                    aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"error": "User not found", "userId": "$userId"}"""),
                ),
        )
    }

    /**
     * 특정 사용자 ID에 대해 서버 에러 응답 stub
     */
    fun stubUserServiceError(userId: UUID) {
        val server = wireMockServer ?: throw IllegalStateException("WireMock server not started")
        server.stubFor(
            get(urlMatching("/internal/v1/users/$userId"))
                .atPriority(1)
                .willReturn(
                    aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"error": "Internal server error"}"""),
                ),
        )
    }
}

/**
 * Spring ApplicationContext 초기화 시 WireMock 서버를 시작하고
 * feign.clients.user-service.url 프로퍼티를 WireMock 서버 주소로 설정
 */
class WireMockInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val server = WireMockUserServiceConfig.startServer()

        TestPropertyValues.of(
            "feign.clients.user-service.url=http://localhost:${server.port()}",
        ).applyTo(applicationContext.environment)

        // Context 종료 시 WireMock 서버 정리
        applicationContext.addApplicationListener { event ->
            if (event is ContextClosedEvent) {
                WireMockUserServiceConfig.stopServer()
            }
        }
    }
}
