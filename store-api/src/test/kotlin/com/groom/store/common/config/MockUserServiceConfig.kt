package com.groom.store.common.config

import com.groom.store.outbound.client.UserResponse
import com.groom.store.outbound.client.UserRole
import com.groom.store.outbound.client.UserServiceClient
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.util.UUID

/**
 * 통합 테스트를 위한 Mock UserServiceClient 설정
 */
@TestConfiguration
class MockUserServiceConfig {
    @Bean
    @Primary
    fun userServiceClient(): UserServiceClient {
        val mock = mockk<UserServiceClient>(relaxed = true)

        // 기본적으로 OWNER 역할을 가진 사용자를 반환하도록 설정
        every { mock.get(any()) } answers {
            val userId = firstArg<UUID>()
            UserResponse(
                id = userId,
                name = "Test User",
                role = UserRole.OWNER,
            )
        }

        return mock
    }
}
