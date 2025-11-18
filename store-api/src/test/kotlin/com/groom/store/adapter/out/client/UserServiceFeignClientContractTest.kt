package com.groom.store.adapter.out.client

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.util.UUID

/**
 * UserServiceFeignClient Contract Test
 *
 * Spring Cloud Contract WireMock을 사용하여 customer-service의 internal API contract를 검증합니다.
 * test resources의 mappings에 정의된 stub을 사용하여 WireMock 서버가 자동으로 실행됩니다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(port = 8081)
@TestPropertySource(
    properties = [
        "feign.clients.user-service.url=http://localhost:8081"
    ]
)
@ActiveProfiles("test")
@DisplayName("UserServiceFeignClient Contract 테스트")
class UserServiceFeignClientContractTest {

    @Autowired
    private lateinit var userServiceFeignClient: UserServiceFeignClient

    @Test
    @DisplayName("특정 유저 정보를 조회할 수 있다")
    fun `should get user information by seller id`() {
        // given
        val sellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        // when
        val result = shouldNotThrowAny {
            userServiceFeignClient.get(sellerId)
        }

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
