package com.groom.store.adapter.inbound.web.dto

import com.groom.store.application.dto.GetStoreInternalResult
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Internal API 스토어 조회 응답 DTO.
 *
 * 다른 마이크로서비스에서 필요로 하는 스토어 정보를 담는다.
 */
data class GetStoreInternalResponse(
    val storeId: UUID,
    val ownerUserId: UUID,
    val name: String,
    val description: String?,
    val status: String,
    val averageRating: BigDecimal?,
    val reviewCount: Int,
    val launchedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(result: GetStoreInternalResult): GetStoreInternalResponse =
            GetStoreInternalResponse(
                storeId = result.storeId,
                ownerUserId = result.ownerUserId,
                name = result.name,
                description = result.description,
                status = result.status.name,
                averageRating = result.averageRating,
                reviewCount = result.reviewCount,
                launchedAt = result.launchedAt,
                createdAt = result.createdAt,
                updatedAt = result.updatedAt,
            )
    }
}
