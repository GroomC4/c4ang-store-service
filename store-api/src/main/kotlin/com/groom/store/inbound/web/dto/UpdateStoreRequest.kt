package com.groom.ecommerce.store.presentation.web.dto

import com.groom.ecommerce.store.application.dto.UpdateStoreCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

/**
 * 스토어 수정 REST 입력 DTO.
 */
@Schema(description = "스토어 수정 요청")
data class UpdateStoreRequest(
    @Schema(description = "스토어명", example = "Updated Store Name", required = true)
    val name: String,
    @Schema(description = "스토어 설명", example = "Updated store description", required = false)
    val description: String?,
) {
    fun toCommand(
        storeId: UUID,
        userId: UUID,
    ): UpdateStoreCommand =
        UpdateStoreCommand(
            storeId = storeId,
            userId = userId,
            name = name,
            description = description,
        )
}
