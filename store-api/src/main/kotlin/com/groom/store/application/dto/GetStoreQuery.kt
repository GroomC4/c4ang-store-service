package com.groom.ecommerce.store.application.dto

import java.util.UUID

/**
 * 스토어 상세 조회 쿼리.
 *
 * @property storeId 조회할 스토어 ID
 */
data class GetStoreQuery(
    val storeId: UUID,
)
