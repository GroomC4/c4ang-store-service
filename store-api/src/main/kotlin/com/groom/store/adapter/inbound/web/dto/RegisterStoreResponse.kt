package com.groom.store.adapter.inbound.web.dto

import com.groom.store.application.dto.RegisterStoreResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 스토어 등록 REST 출력 DTO.
 */
@Schema(description = "스토어 등록 응답")
data class RegisterStoreResponse(
    @Schema(description = "스토어 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    val storeId: String,
    @Schema(description = "스토어명", example = "My Store")
    val name: String,
    @Schema(description = "스토어 설명", example = "This is my store")
    val description: String?,
    @Schema(description = "스토어 상태", example = "PREPARING")
    val status: String,
    @Schema(description = "생성 시각", example = "2025-10-29T10:00:00")
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(result: RegisterStoreResult): RegisterStoreResponse =
            RegisterStoreResponse(
                storeId = result.storeId,
                name = result.name,
                description = result.description,
                status = result.status,
                createdAt = result.createdAt,
            )
    }
}
