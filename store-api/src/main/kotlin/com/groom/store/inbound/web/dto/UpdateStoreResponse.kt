package com.groom.ecommerce.store.presentation.web.dto

import com.groom.ecommerce.store.application.dto.UpdateStoreResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 스토어 수정 REST 출력 DTO.
 */
@Schema(description = "스토어 수정 응답")
data class UpdateStoreResponse(
    @Schema(description = "스토어 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    val storeId: String,
    @Schema(description = "스토어명", example = "Updated Store Name")
    val name: String,
    @Schema(description = "스토어 설명", example = "Updated store description")
    val description: String?,
    @Schema(description = "스토어 상태", example = "PREPARING")
    val status: String,
    @Schema(description = "수정 시각", example = "2025-10-29T10:30:00")
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(result: UpdateStoreResult): UpdateStoreResponse =
            UpdateStoreResponse(
                storeId = result.storeId,
                name = result.name,
                description = result.description,
                status = result.status,
                updatedAt = result.updatedAt,
            )
    }
}
