package com.groom.store.adapter.inbound.web.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Internal API 에러 응답 DTO.
 *
 * 다른 마이크로서비스에서 사용하는 통일된 에러 응답 형식입니다.
 */
@Schema(description = "Internal API 에러 응답")
data class InternalApiErrorResponse(
    @Schema(description = "에러 코드")
    val error: String,
    @Schema(description = "에러 메세지")
    val message: String,
)
