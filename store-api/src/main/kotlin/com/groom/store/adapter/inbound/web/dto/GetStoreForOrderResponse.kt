package com.groom.store.adapter.inbound.web.dto

import com.groom.store.application.dto.GetStoreForOrderResult
import java.time.LocalDateTime
import java.util.UUID

/**
 * Order Service용 스토어 조회 응답 DTO.
 *
 * Order Service에서 필요로 하는 스토어 정보를 포함.
 */
data class GetStoreForOrderResponse(
    val storeId: UUID,
    val ownerUserId: UUID,
    val name: String,
    val status: String,
    val reviewCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(result: GetStoreForOrderResult): GetStoreForOrderResponse =
            GetStoreForOrderResponse(
                storeId = result.storeId,
                ownerUserId = result.ownerUserId,
                name = result.name,
                status = result.status,
                reviewCount = result.reviewCount,
                createdAt = result.createdAt,
                updatedAt = result.updatedAt,
            )
    }
}
