package com.groom.ecommerce.store.application.dto

import java.time.LocalDateTime

/**
 * 스토어 수정 유스케이스 출력 모델.
 */
data class UpdateStoreResult(
    val storeId: String,
    val ownerUserId: String,
    val name: String,
    val description: String?,
    val status: String,
    val updatedAt: LocalDateTime,
)
