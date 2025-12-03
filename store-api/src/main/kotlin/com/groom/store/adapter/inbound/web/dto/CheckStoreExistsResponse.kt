package com.groom.store.adapter.inbound.web.dto

import com.groom.store.application.dto.CheckStoreExistsResult

/**
 * 스토어 존재 여부 확인 응답 DTO.
 */
data class CheckStoreExistsResponse(
    val exists: Boolean,
) {
    companion object {
        fun from(result: CheckStoreExistsResult): CheckStoreExistsResponse =
            CheckStoreExistsResponse(
                exists = result.exists,
            )
    }
}
