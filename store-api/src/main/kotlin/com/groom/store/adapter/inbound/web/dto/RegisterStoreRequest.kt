package com.groom.store.adapter.inbound.web.dto

import com.groom.store.application.dto.RegisterStoreCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

/**
 * 스토어 등록 REST 입력 DTO.
 */
@Schema(description = "스토어 등록 요청")
data class RegisterStoreRequest(
    @Schema(description = "스토어명", example = "My Store", required = true)
    val name: String,
    @Schema(description = "스토어 설명", example = "This is my store", required = false)
    val description: String?,
) {
    fun toCommand(ownerUserId: UUID): RegisterStoreCommand =
        RegisterStoreCommand(
            ownerUserId = ownerUserId,
            name = name,
            description = description,
        )
}
