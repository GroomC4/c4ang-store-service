package com.groom.store.infrastructure.event

import com.groom.ecommerce.store.event.avro.StoreInfoUpdated
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * StoreEventPublisher 사용 예제
 *
 * 실제 사용 시 이 클래스를 참고하여 구현하세요.
 */
@Component
class StoreEventPublisherExample(
    private val storeEventPublisher: StoreEventPublisher,
) {
    /**
     * 스토어 이름 변경 시 이벤트 발행 예제
     */
    fun publishStoreNameChanged(
        storeId: String,
        newStoreName: String,
        storeStatus: String = "ACTIVE",
    ) {
        val event = StoreInfoUpdated.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setEventTimestamp(System.currentTimeMillis())
            .setStoreId(storeId)
            .setStoreName(newStoreName)
            .setStoreStatus(storeStatus)
            .setStoreDescription(null)
            .setStorePhone(null)
            .setStoreAddress(null)
            .setBusinessHours(null)
            .setStoreImageUrl(null)
            .setUpdatedFields(listOf("storeName"))
            .setUpdatedAt(System.currentTimeMillis())
            .build()

        storeEventPublisher.publishStoreInfoUpdated(event)
    }

    /**
     * 스토어 전체 정보 변경 시 이벤트 발행 예제
     */
    fun publishStoreFullInfoUpdated(
        storeId: String,
        storeName: String,
        storeStatus: String,
        description: String?,
        phone: String?,
    ) {
        val event = StoreInfoUpdated.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setEventTimestamp(System.currentTimeMillis())
            .setStoreId(storeId)
            .setStoreName(storeName)
            .setStoreStatus(storeStatus)
            .setStoreDescription(description)
            .setStorePhone(phone)
            .setStoreAddress(null) // Address 객체가 필요하면 별도 생성
            .setBusinessHours(null)
            .setStoreImageUrl(null)
            .setUpdatedFields(listOf("storeName", "storeStatus", "description", "phone"))
            .setUpdatedAt(System.currentTimeMillis())
            .build()

        storeEventPublisher.publishStoreInfoUpdated(event)
    }
}
