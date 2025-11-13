package com.groom.ecommerce.store.presentation.web.dto

import com.groom.ecommerce.store.application.dto.DeleteStoreResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 스토어 삭제 응답 DTO.
 *
 * @property storeId 삭제된 스토어 ID
 * @property ownerUserId 스토어 소유자 ID
 * @property name 스토어명
 * @property deletedAt 삭제 시각
 */
@Schema(description = "스토어 삭제 응답")
data class DeleteStoreResponse(
    @Schema(description = "삭제된 스토어 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    val storeId: String,
    @Schema(description = "스토어 소유자 ID", example = "660e8400-e29b-41d4-a716-446655440001")
    val ownerUserId: String,
    @Schema(description = "스토어명", example = "My Store")
    val name: String,
    @Schema(description = "삭제 시각", example = "2025-10-29T11:00:00")
    val deletedAt: LocalDateTime,
) {
    companion object {
        fun from(result: DeleteStoreResult): DeleteStoreResponse =
            DeleteStoreResponse(
                storeId = result.storeId,
                ownerUserId = result.ownerUserId,
                name = result.name,
                deletedAt = result.deletedAt,
            )
    }
}
