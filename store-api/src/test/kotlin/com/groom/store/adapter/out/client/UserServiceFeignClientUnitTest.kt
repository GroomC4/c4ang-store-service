package com.groom.store.adapter.out.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.groom.ecommerce.customer.api.avro.UserRole as ContractUserRole
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
 * UserServiceFeignClient Unit Test
 *
 * 목적: FeignClient 자체의 동작을 검증하는 빠른 단위 테스트
 *
 * - WireMock을 사용하여 HTTP 통신 및 데이터 직렬화/역직렬화 검증
 * - Spring Context 없이 독립적으로 실행 (빠른 피드백)
 * - FeignClient 설정 및 에러 핸들링 검증
 * - Avro 스키마 기반 응답 파싱 검증
 *
 * 주의: 이 테스트는 customer-service와의 실제 계약을 검증하지 않습니다.
 * 실제 API 계약 검증은 UserServiceFeignClientConsumerContractTest를 참고하세요.
 */
@DisplayName("UserServiceFeignClient 단위 테스트")
class UserServiceFeignClientUnitTest {

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

        // Stub 설정 - UserInternalResponse 스키마에 맞춘 응답
        wireMockServer.stubFor(
            get(urlEqualTo("/internal/v1/users/550e8400-e29b-41d4-a716-446655440000"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "userId": "550e8400-e29b-41d4-a716-446655440000",
                                "username": "test_user",
                                "email": "test@example.com",
                                "role": "OWNER",
                                "isActive": true,
                                "profile": {
                                    "fullName": "Test User",
                                    "phoneNumber": "+82-10-1234-5678",
                                    "address": "서울시 강남구 테헤란로 123"
                                },
                                "createdAt": 1699000000000,
                                "updatedAt": 1699999999999,
                                "lastLoginAt": 1699999999999
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
    @DisplayName("FeignClient가 HTTP 요청을 올바르게 전송하고 응답을 Avro 객체로 역직렬화한다")
    fun `should deserialize HTTP response to Avro UserInternalResponse`() {
        // given
        val userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        // when
        val result = userServiceFeignClient.get(userId)

        // then - Avro 객체로 올바르게 역직렬화 검증
        result shouldNotBe null
        result.getUserId() shouldBe userId.toString()
        result.getUsername() shouldBe "test_user"
        result.getEmail() shouldBe "test@example.com"
        result.getIsActive() shouldBe true
        result.getProfile() shouldNotBe null
        result.getProfile().getFullName() shouldBe "Test User"
        result.getProfile().getPhoneNumber() shouldBe "+82-10-1234-5678"
    }

    @Test
    @DisplayName("FeignClient가 Avro Enum 타입을 올바르게 파싱한다")
    fun `should correctly parse Avro enum type for UserRole`() {
        // given
        val userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        // when
        val result = userServiceFeignClient.get(userId)

        // then - Avro Enum 타입 검증
        result.getRole() shouldBe ContractUserRole.OWNER
    }
}
