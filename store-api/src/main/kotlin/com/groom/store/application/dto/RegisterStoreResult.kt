package com.groom.store.application.dto

import java.time.LocalDateTime

/**
 * 스토어 등록 유스케이스 출력 모델.
 */
data class RegisterStoreResult(
    val storeId: String,
    val ownerUserId: String,
    val name: String,
    val description: String?,
    val status: String,
    val createdAt: LocalDateTime,
)
