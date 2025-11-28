package com.groom.store.adapter.out.event.dto

/**
 * Kafka로 발행되는 Store 정보 업데이트 이벤트 DTO
 */
data class StoreInfoUpdatedEventDto(
    val eventId: String,
    val eventTimestamp: Long,
    val storeId: String,
    val storeName: String,
    val storeStatus: String,
    val storeDescription: String?,
    val storePhone: String?,
    val storeAddress: String?,
    val businessHours: String?,
    val storeImageUrl: String?,
    val updatedFields: List<String>,
    val updatedAt: Long,
)
