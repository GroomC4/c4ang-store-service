package com.groom.store.adapter.out.client

import com.groom.store.adapter.out.client.dto.UserInternalDto
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

/**
 * user 서비스와 통신하기 위한 Feign Client
 *
 * Customer Service의 Internal API와 통신합니다.
 *
 * 테스트 환경에서는 MockUserServiceClient가 사용됩니다.
 */
@FeignClient(
    name = "user-service",
    url = "\${feign.clients.user-service.url:http://localhost:8081}",
)
@Profile("!test")
@ConditionalOnMissingBean(UserServiceClient::class)
interface UserServiceFeignClient : UserServiceClient {
    /**
     * 특정 유저 정보 조회
     *
     * Customer Service의 Internal User API를 호출합니다.
     * 엔드포인트: GET /internal/v1/users/{userId}
     *
     * @param sellerId 판매자 ID (UUID)
     * @return UserInternalDto - 사용자 정보
     */
    @GetMapping("/internal/v1/users/{sellerId}")
    override fun get(
        @PathVariable sellerId: UUID,
    ): UserInternalDto
}
