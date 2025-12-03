package com.groom.store.adapter.inbound.web.dto

import com.groom.store.application.dto.GetStoreForOrderResult
import java.util.UUID

/**
 * Order Service용 스토어 조회 응답 DTO.
 *
 * Order Service에서 필요로 하는 최소한의 스토어 정보만 포함.
 */
data class GetStoreForOrderResponse(
    val id: UUID,
    val name: String,
    val status: String,
) {
    companion object {
        fun from(result: GetStoreForOrderResult): GetStoreForOrderResponse =
            GetStoreForOrderResponse(
                id = result.id,
                name = result.name,
                status = result.status,
            )
    }
}
