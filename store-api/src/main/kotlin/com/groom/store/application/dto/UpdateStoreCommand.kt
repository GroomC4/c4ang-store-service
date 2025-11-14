package com.groom.store.application.dto

import java.util.UUID

/**
 * 스토어 수정 유스케이스 입력 모델.
 */
data class UpdateStoreCommand(
    val storeId: UUID,
    val userId: UUID,
    val name: String,
    val description: String?,
)
