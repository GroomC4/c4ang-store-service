package com.groom.store.application.dto

import java.util.UUID

/**
 * 스토어 ID로 내부 조회 쿼리.
 */
data class GetStoreByIdQuery(
    val storeId: UUID,
)

/**
 * 소유자 ID로 스토어 내부 조회 쿼리.
 */
data class GetStoreByOwnerIdQuery(
    val ownerUserId: UUID,
)
