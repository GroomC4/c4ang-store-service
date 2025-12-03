package com.groom.store.application.dto

import com.groom.store.domain.model.Store
import java.time.LocalDateTime
import java.util.UUID

/**
 * Order Service용 스토어 조회 결과 DTO.
 *
 * Order Service에서 필요로 하는 스토어 정보를 포함.
 */
data class GetStoreForOrderResult(
    val storeId: UUID,
    val ownerUserId: UUID,
    val name: String,
    val status: String,
    val reviewCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(store: Store): GetStoreForOrderResult =
            GetStoreForOrderResult(
                storeId = store.id,
                ownerUserId = store.ownerUserId,
                name = store.name,
                status = store.status.name,
                reviewCount = store.rating?.reviewCount ?: 0,
                createdAt = store.createdAt ?: LocalDateTime.now(),
                updatedAt = store.updatedAt ?: LocalDateTime.now(),
            )
    }
}
