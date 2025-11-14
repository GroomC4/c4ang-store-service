package com.groom.store.application.dto

import java.util.UUID

/**
 * 스토어 등록 유스케이스 입력 모델.
 */
data class RegisterStoreCommand(
    val ownerUserId: UUID,
    val name: String,
    val description: String?,
)
