package com.groom.ecommerce.store.application.dto

import com.groom.ecommerce.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 스토어 상세 조회 결과 DTO.
 *
 * @property storeId 스토어 ID
 * @property ownerUserId 스토어 소유자 ID
 * @property name 스토어명
 * @property description 스토어 설명
 * @property status 스토어 상태
 * @property averageRating 평균 평점
 * @property reviewCount 총 리뷰 수
 * @property launchedAt 스토어 출시 시각
 * @property createdAt 생성 시각
 * @property updatedAt 수정 시각
 */
data class GetStoreResult(
    val storeId: String,
    val ownerUserId: String,
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
        fun from(store: Store): GetStoreResult =
            GetStoreResult(
                storeId = store.id.toString(),
                ownerUserId = store.ownerUserId.toString(),
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
