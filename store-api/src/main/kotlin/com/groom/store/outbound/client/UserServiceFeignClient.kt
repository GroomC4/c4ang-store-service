package com.groom.store.outbound.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

/**
 * user 서비스와 통신하기 위한 Feign Client
 *
 * 테스트 환경에서는 MockStoreClient를 사용하므로 이 빈은 생성되지 않습니다.
 */
@Profile("!test")
@FeignClient(
    name = "store-service",
    url = "\${feign.clients.store-service.url:http://localhost:8081}",
)
interface UserServiceFeignClient : UserServiceClient {
    /**
     * 특정 유저 정보 조회
     *
     * @param sellerId 판매자 ID
     * @return 유저 정보
     */
    @GetMapping("/api/internal/user/{sellerId}")
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
