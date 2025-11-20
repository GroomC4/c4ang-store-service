package com.groom.store.fixture

import com.groom.ecommerce.customer.api.avro.UserInternalResponse
import com.groom.ecommerce.customer.api.avro.UserProfileInternal
import com.groom.store.adapter.out.client.UserServiceClient
import com.groom.store.domain.model.UserRole
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.*

/**
 * 테스트용 Mock UserServiceClient 구현체
 *
 * TestUserRegistry에 등록된 사용자 정보를 기반으로
 * UserInternalResponse (Avro 생성 클래스)를 반환합니다.
 *
 * 도메인 UserRole을 Avro UserRole로 변환하여 반환합니다.
 */
@Component
@Profile("test")
@Primary
class MockUserServiceClient : UserServiceClient {

    override fun get(sellerId: UUID): UserInternalResponse {
        val testUser = TestUserRegistry.getUser(sellerId)

        // 도메인 UserRole을 Contract UserRole로 변환
        val contractRole = when (testUser.role) {
            UserRole.CUSTOMER -> com.groom.ecommerce.customer.api.avro.UserRole.CUSTOMER
            UserRole.OWNER -> com.groom.ecommerce.customer.api.avro.UserRole.OWNER
            // MANAGER, MASTER는 contract hub에 없으므로 CUSTOMER로 매핑
            UserRole.MANAGER -> com.groom.ecommerce.customer.api.avro.UserRole.CUSTOMER
            UserRole.MASTER -> com.groom.ecommerce.customer.api.avro.UserRole.CUSTOMER
        }

        // UserInternalResponse Builder 패턴 사용
        val profile = UserProfileInternal.newBuilder()
            .setFullName(testUser.name)
            .setPhoneNumber("+82-10-1234-5678")
            .setAddress("서울시 강남구 테헤란로 123")
            .build()

        return UserInternalResponse.newBuilder()
            .setUserId(testUser.id.toString())
            .setUsername(testUser.name.lowercase().replace(" ", "_"))
            .setEmail("${testUser.name.lowercase().replace(" ", "_")}@example.com")
            .setRole(contractRole)
            .setIsActive(true)
            .setProfile(profile)
            .setCreatedAt(System.currentTimeMillis() - 86400000L) // 1일 전
            .setUpdatedAt(System.currentTimeMillis())
            .setLastLoginAt(System.currentTimeMillis())
            .build()
    }
}