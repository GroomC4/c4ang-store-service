package com.groom.store.adapter.out.client

import com.groom.ecommerce.customer.api.avro.UserRole as ContractUserRole
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

/**
 * UserServiceFeignClient Spring Cloud Contract Stub Runner Test
 *
 * customer-service에서 발행한 Contract Stub을 사용하여
 * UserServiceFeignClient의 명세를 검증합니다.
 *
 * 이 테스트는 customer-service가 로컬 Maven 레포지토리에 발행한
 * Contract Stub JAR를 자동으로 다운로드하여 WireMock 서버를 실행합니다.
 */
@SpringBootTest
@AutoConfigureStubRunner(
    ids = ["com.groom:customer-service-contract-stubs:+:stubs:8090"],
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@ActiveProfiles("test")
@DisplayName("UserServiceFeignClient Spring Cloud Contract Stub 테스트")
class UserServiceFeignClientStubRunnerTest {

    @Autowired
    private lateinit var userServiceFeignClient: UserServiceFeignClient

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
    @DisplayName("Contract Stub을 통해 존재하지 않는 사용자 조회시 예외가 발생한다")
    fun `should handle user not found from contract stub`() {
        // given - 존재하지 않는 사용자 ID
        val nonExistentUserId = UUID.randomUUID()

        // when & then
        try {
            userServiceFeignClient.get(nonExistentUserId)
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: Exception) {
            // FeignException 발생 예상
            e.message shouldNotBe null
        }
    }
}