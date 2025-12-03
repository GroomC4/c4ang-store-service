package com.groom.store.application.dto

import com.groom.store.domain.model.Store
import java.util.UUID

/**
 * Order Service용 스토어 조회 결과 DTO.
 *
 * Order Service에서 필요로 하는 최소한의 스토어 정보만 포함.
 */
data class GetStoreForOrderResult(
    val id: UUID,
    val name: String,
    val status: String,
) {
    companion object {
        fun from(store: Store): GetStoreForOrderResult =
            GetStoreForOrderResult(
                id = store.id,
                name = store.name,
                status = store.status.name,
            )
    }
}
