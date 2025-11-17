package com.groom.store.adapter.out.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

/**
 * user 서비스와 통신하기 위한 Feign Client
 *
 * 테스트 환경에서는 Spring Cloud Contract Stub Runner를 통해 WireMock stub과 통신합니다.
 */
@FeignClient(
    name = "user-service",
    url = "\${feign.clients.user-service.url:http://localhost:8081}",
)
interface UserServiceFeignClient : UserServiceClient {
    /**
     * 특정 유저 정보 조회
     *
     * @param sellerId 판매자 ID
     * @return 유저 정보
     */
    @GetMapping("/api/internal/users/{sellerId}")
    override fun get(
        @PathVariable sellerId: UUID,
    ): UserResponse
}

/**
 * 유저 정보 응답 DTO
 */
data class UserResponse(
    val id: UUID,
    val name: String,
    val role: UserRole,
)

enum class UserRole {
    CUSTOMER,
    OWNER,
    MANAGER,
    MASTER,
}
