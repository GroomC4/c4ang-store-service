package com.groom.store.application.dto

import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * 스토어 내부 조회 결과 DTO.
 *
 * 다른 마이크로서비스에서 필요로 하는 스토어 정보를 담는다.
 */
data class GetStoreInternalResult(
    val storeId: UUID,
    val ownerUserId: UUID,
    val name: String,
    val description: String?,
    val status: StoreStatus,
    val averageRating: BigDecimal?,
    val reviewCount: Int,
    val launchedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(store: Store): GetStoreInternalResult =
            GetStoreInternalResult(
                storeId = store.id,
                ownerUserId = store.ownerUserId,
                name = store.name,
                description = store.description,
                status = store.status,
                averageRating = store.rating?.averageRating,
                reviewCount = store.rating?.reviewCount ?: 0,
                launchedAt = store.rating?.launchedAt,
                createdAt = store.createdAt!!,
                updatedAt = store.updatedAt!!,
            )
    }
}
