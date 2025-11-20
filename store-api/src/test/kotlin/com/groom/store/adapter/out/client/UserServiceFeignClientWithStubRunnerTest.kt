package com.groom.store.adapter.out.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.groom.ecommerce.customer.api.avro.UserRole as ContractUserRole
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.cloud.openfeign.support.SpringMvcContract
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.util.UUID

/**
 * UserServiceFeignClient with Spring Cloud Contract Stub Runner Test
 *
 * customer-service에서 발행한 Contract Stub을 사용하여
 * UserServiceFeignClient의 명세를 검증합니다.
 *
 * 이 테스트는 customer-service가 로컬 Maven 레포지토리에 발행한
 * Contract Stub JAR를 자동으로 다운로드하여 WireMock 서버를 실행하고,
 * Feign Client를 직접 생성하여 테스트합니다.
 */
@SpringJUnitConfig
@AutoConfigureStubRunner(
    ids = ["com.groom:customer-service-contract-stubs:+:stubs:8090"],
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@ActiveProfiles("test")
@DisplayName("UserServiceFeignClient with Contract Stub 테스트")
class UserServiceFeignClientWithStubRunnerTest {

    private lateinit var userServiceFeignClient: UserServiceFeignClient

    @BeforeEach
    fun setup() {
        val objectMapper = ObjectMapper().registerKotlinModule()

        // Feign Client를 Stub Runner가 실행한 WireMock 서버에 연결
        userServiceFeignClient = Feign.builder()
            .contract(SpringMvcContract())
            .encoder(JacksonEncoder(objectMapper))
            .decoder(JacksonDecoder(objectMapper))
            .requestInterceptor { template ->
                template.header("Content-Type", "application/json")
            }
            .target(UserServiceFeignClient::class.java, "http://localhost:8090")
    }

    @Test
    @DisplayName("Contract Stub을 통해 유효한 사용자 정보를 조회할 수 있다")
    fun `should get user information from contract stub`() {
        // given - customer-service contract에 정의된 사용자 ID
        val userId = UUID.fromString("750e8400-e29b-41d4-a716-446655440001")

        // when
        val result = userServiceFeignClient.get(userId)

        // then - contract에 정의된 응답 검증
        result shouldNotBe null
        result.getUserId() shouldBe "750e8400-e29b-41d4-a716-446655440001"
        result.getUsername() shouldBe "고객테스트"
        result.getEmail() shouldBe "customer@example.com"
        result.getRole() shouldBe ContractUserRole.CUSTOMER
        result.getIsActive() shouldBe true

        // profile 정보 검증
        result.getProfile() shouldNotBe null
        result.getProfile().getFullName() shouldBe "고객테스트"
        result.getProfile().getPhoneNumber() shouldBe "010-1111-2222"
    }

    @Test
    @DisplayName("Contract Stub을 통해 존재하지 않는 사용자 조회시 404 에러가 발생한다")
    fun `should return 404 for non-existent user from contract stub`() {
        // given - 존재하지 않는 사용자 ID (contract에 정의되지 않은 ID)
        val nonExistentUserId = UUID.randomUUID()

        // when & then
        try {
            userServiceFeignClient.get(nonExistentUserId)
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: Exception) {
            // FeignException.NotFound 또는 404 관련 예외 예상
            e.message shouldNotBe null
        }
    }
}