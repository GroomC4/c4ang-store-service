package com.groom.store.application.dto

import java.util.UUID

/**
 * Order Service용 스토어 조회 쿼리 DTO.
 *
 * Order Service에서 주문 생성 시 스토어 정보를 조회할 때 사용.
 */
data class GetStoreForOrderQuery(
    val storeId: UUID,
)
