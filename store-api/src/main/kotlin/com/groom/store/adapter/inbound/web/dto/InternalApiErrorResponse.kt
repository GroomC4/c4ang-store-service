package com.groom.store.adapter.inbound.web.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Internal API 에러 응답 DTO.
 *
 * 다른 마이크로서비스에서 사용하는 통일된 에러 응답 형식입니다.
 */
@Schema(description = "Internal API 에러 응답")
data class InternalApiErrorResponse(
    @Schema(description = "에러 코드", example = "STORE_NOT_FOUND")
    val errorCode: String,
    @Schema(description = "에러 메세지", example = "스토어를 찾을 수 없습니다")
    val message: String,
    @Schema(description = "에러 발생 시간 (Unix timestamp in millis)", example = "1699999999999")
    val timestamp: Long,
    @Schema(description = "요청 경로", example = "/internal/v1/stores/550e8400-e29b-41d4-a716-446655440000")
    val path: String,
)
