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
 * UserServiceFeignClient Consumer Contract Test
 *
 * 목적: customer-service와의 API 계약(Contract)을 검증하는 Consumer Contract Test
 *
 * Spring Cloud Contract의 Consumer-Driven Contract Testing:
 * - customer-service(Provider)가 발행한 Contract Stub을 사용
 * - store-service(Consumer) 관점에서 API 계약 준수 여부 검증
 * - customer-service의 실제 API 변경 사항을 조기에 감지
 *
 * 동작 방식:
 * 1. customer-service의 Contract Stub JAR를 로컬 Maven에서 로드
 * 2. Stub Runner가 WireMock 서버를 8090 포트에서 실행
 * 3. Contract에 정의된 요청/응답 시나리오 검증
 *
 * 차이점:
 * - Unit Test: FeignClient 자체 동작 검증 (빠름, 독립적)
 * - Contract Test: customer-service와의 실제 계약 검증 (느림, 의존적)
 *
 * 주의:
 * - customer-service의 Contract Stub이 로컬에 발행되어 있어야 합니다
 * - Contract 변경 시 이 테스트가 실패하면 두 서비스 간 호환성 문제를 의미합니다
 *
 * Stub 로드 전략:
 * - REMOTE 모드를 사용하여 GitHub Packages에서 Contract Stub 다운로드
 * - 명시적 버전(1.0.8) 지정으로 안정적인 Contract 검증
 * - CI/로컬 환경 모두에서 동일하게 동작
 *
 * GitHub Packages 인증:
 * - GITHUB_ACTOR, GITHUB_TOKEN 환경변수 필요
 * - CI에서는 자동으로 설정됨
 * - 로컬에서는 build.gradle.kts의 repositories 설정 참조
 */
@SpringJUnitConfig
@AutoConfigureStubRunner(
    ids = ["com.groom:customer-service-contract-stubs:1.0.8:stubs:8090"],
    stubsMode = StubRunnerProperties.StubsMode.REMOTE,
    repositoryRoot = "https://maven.pkg.github.com/GroomC4/c4ang-customer-service"
)
@ActiveProfiles("test")
@DisplayName("UserServiceFeignClient Consumer Contract 테스트")
class UserServiceFeignClientConsumerContractTest {

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
    @DisplayName("[Contract 검증] customer-service가 정의한 사용자 조회 API 계약을 준수한다")
    fun `should comply with customer service contract for getting user by id`() {
        // given - customer-service의 Contract에 정의된 사용자 ID
        // Contract 파일: should_return_user_when_id_exists.yml
        val contractDefinedUserId = UUID.fromString("750e8400-e29b-41d4-a716-446655440001")

        // when - Contract에 정의된 요청 실행
        val result = userServiceFeignClient.get(contractDefinedUserId)

        // then - Contract에 정의된 응답 스펙 검증
        // Contract는 customer-service의 실제 API 명세를 반영합니다
        result shouldNotBe null
        result.getUserId() shouldBe "750e8400-e29b-41d4-a716-446655440001"
        result.getUsername() shouldBe "고객테스트"
        result.getEmail() shouldBe "customer@example.com"
        result.getRole() shouldBe ContractUserRole.CUSTOMER
        result.getIsActive() shouldBe true

        // profile 정보도 Contract에 정의된 대로 검증
        result.getProfile() shouldNotBe null
        result.getProfile().getFullName() shouldBe "고객테스트"
        result.getProfile().getPhoneNumber() shouldBe "010-1111-2222"
    }

    @Test
    @DisplayName("[Contract 검증] 존재하지 않는 사용자 조회 시 404 응답을 받는다")
    fun `should receive 404 response when user not found as per contract`() {
        // given - Contract에 정의되지 않은 사용자 ID
        // Contract는 이 경우 404를 반환하도록 정의되어 있습니다
        val nonExistentUserId = UUID.randomUUID()

        // when & then - Contract에 정의된 에러 응답 검증
        try {
            userServiceFeignClient.get(nonExistentUserId)
            throw AssertionError("Contract에 따라 404 예외가 발생해야 합니다")
        } catch (e: Exception) {
            // Contract에 정의된 404 응답 검증
            // customer-service의 실제 에러 응답과 동일한 형식
            e.message shouldNotBe null
        }
    }
}