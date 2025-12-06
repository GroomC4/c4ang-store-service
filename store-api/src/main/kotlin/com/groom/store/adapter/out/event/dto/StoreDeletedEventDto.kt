package com.groom.store.adapter.out.event.dto

/**
 * Kafka로 발행되는 Store 삭제 이벤트 DTO
 */
data class StoreDeletedEventDto(
    val eventId: String,
    val eventTimestamp: Long,
    val storeId: String,
    val ownerUserId: String,
    val storeName: String,
    val deletedAt: Long,
)
