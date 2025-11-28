package com.groom.store.fixture

import com.groom.store.adapter.out.client.UserServiceClient
import com.groom.store.adapter.out.client.dto.UserInternalDto
import com.groom.store.adapter.out.client.dto.UserProfileDto
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * 테스트용 Mock UserServiceClient 구현체
 *
 * TestUserRegistry에 등록된 사용자 정보를 기반으로 UserInternalDto를 반환합니다.
 */
@Component
@Profile("test")
@Primary
class MockUserServiceClient : UserServiceClient {
    override fun get(sellerId: UUID): UserInternalDto {
        val testUser = TestUserRegistry.getUser(sellerId)

        val profile =
            UserProfileDto(
                fullName = testUser.name,
                phoneNumber = "+82-10-1234-5678",
                address = "서울시 강남구 테헤란로 123",
            )

        return UserInternalDto(
            userId = testUser.id.toString(),
            username = testUser.name.lowercase().replace(" ", "_"),
            email = "${testUser.name.lowercase().replace(" ", "_")}@example.com",
            role = testUser.role.name,
            isActive = true,
            profile = profile,
            createdAt = System.currentTimeMillis() - 86400000L,
            updatedAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis(),
        )
    }
}