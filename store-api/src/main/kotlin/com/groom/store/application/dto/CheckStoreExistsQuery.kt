package com.groom.store.application.dto

import java.util.UUID

/**
 * 스토어 존재 여부 확인 쿼리 DTO.
 */
data class CheckStoreExistsQuery(
    val storeId: UUID,
)
