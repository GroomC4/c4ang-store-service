package com.groom.store.adapter.out.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.cloud.openfeign.support.SpringMvcContract
import java.util.UUID

/**
 * UserServiceFeignClient Contract Test
 *
 * WireMock을 사용하여 customer-service의 internal API contract를 검증합니다.
 * Feign 클라이언트를 직접 구성하여 전체 Spring Context 로드 없이 테스트합니다.
 */
@DisplayName("UserServiceFeignClient Contract 테스트")
class UserServiceFeignClientContractTest {

    private lateinit var wireMockServer: WireMockServer
    private lateinit var userServiceFeignClient: UserServiceFeignClient

    @BeforeEach
    fun setup() {
        // WireMock 서버 시작
        wireMockServer = WireMockServer(WireMockConfiguration.options().port(8081))
        wireMockServer.start()

        // ObjectMapper with Kotlin module
        val objectMapper = ObjectMapper().registerKotlinModule()

        // Feign 클라이언트 구성 (Spring MVC annotations 지원, Kotlin data class 지원)
        userServiceFeignClient = Feign.builder()
            .contract(SpringMvcContract())
            .encoder(JacksonEncoder(objectMapper))
            .decoder(JacksonDecoder(objectMapper))
            .target(UserServiceFeignClient::class.java, "http://localhost:8081")

        // Stub 설정
        wireMockServer.stubFor(
            get(urlEqualTo("/api/internal/users/550e8400-e29b-41d4-a716-446655440000"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "id": "550e8400-e29b-41d4-a716-446655440000",
                                "name": "Test User",
                                "role": "OWNER"
                            }
                        """.trimIndent())
                )
        )
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    @Test
    @DisplayName("특정 유저 정보를 조회할 수 있다")
    fun `should get user information by seller id`() {
        // given
        val sellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        // when
        val result = userServiceFeignClient.get(sellerId)

        // then
        result shouldNotBe null
        result.id shouldBe sellerId
        result.name shouldNotBe null
        result.role shouldNotBe null
    }

    @Test
    @DisplayName("OWNER 역할을 가진 유저 정보를 조회할 수 있다")
    fun `should get user information with OWNER role`() {
        // given
        val sellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        // when
        val result = userServiceFeignClient.get(sellerId)

        // then
        result.role shouldBe UserRole.OWNER
    }
}
