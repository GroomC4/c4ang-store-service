package com.groom.store.adapter.inbound.web.dto

import com.groom.store.application.dto.GetStoreResult
import com.groom.store.common.enums.StoreStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 스토어 상세 조회 응답 DTO.
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
@Schema(description = "스토어 상세 조회 응답")
data class GetStoreResponse(
    @Schema(description = "스토어 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    val storeId: String,
    @Schema(description = "스토어 소유자 ID", example = "660e8400-e29b-41d4-a716-446655440001")
    val ownerUserId: String,
    @Schema(description = "스토어명", example = "My Store")
    val name: String,
    @Schema(description = "스토어 설명", example = "This is my store")
    val description: String?,
    @Schema(description = "스토어 상태", example = "LAUNCHED")
    val status: StoreStatus,
    @Schema(description = "평균 평점", example = "4.5")
    val averageRating: BigDecimal?,
    @Schema(description = "총 리뷰 수", example = "120")
    val reviewCount: Int,
    @Schema(description = "스토어 출시 시각", example = "2025-10-29T09:00:00")
    val launchedAt: LocalDateTime?,
    @Schema(description = "생성 시각", example = "2025-10-29T10:00:00")
    val createdAt: LocalDateTime,
    @Schema(description = "수정 시각", example = "2025-10-29T10:30:00")
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(result: GetStoreResult): GetStoreResponse =
            GetStoreResponse(
                storeId = result.storeId,
                ownerUserId = result.ownerUserId,
                name = result.name,
                description = result.description,
                status = result.status,
                averageRating = result.averageRating,
                reviewCount = result.reviewCount,
                launchedAt = result.launchedAt,
                createdAt = result.createdAt,
                updatedAt = result.updatedAt,
            )
    }
}
