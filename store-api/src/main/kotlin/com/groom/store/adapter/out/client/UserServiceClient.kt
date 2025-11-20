package com.groom.store.adapter.out.client

import com.groom.ecommerce.customer.api.avro.UserInternalResponse
import java.util.UUID

/**
 * user 서비스와 통신하기 위한 추상 인터페이스
 *
 * 구현체:
 * - UserServiceFeignClient: REST API 통신 (현재)
 * - UserServiceGrpcClient: gRPC 통신 (향후 추가 가능)
 *
 * 이 인터페이스를 통해 통신 방식을 추상화하여,
 * 향후 REST에서 gRPC로 변경 시 Adapter 코드 수정 없이 전환 가능합니다.
 */
interface UserServiceClient {
    /**
     * 특정 유저 정보 조회
     */
    fun get(sellerId: UUID): UserInternalResponse
}
