package com.groom.store.domain.port

import com.groom.store.domain.event.StoreCreatedEvent
import com.groom.store.domain.event.StoreDeletedEvent
import com.groom.store.domain.event.StoreInfoUpdatedEvent

/**
 * 도메인 이벤트 발행을 위한 Outbound Port.
 * Domain이 외부 메시징 시스템(Kafka 등)에 요구하는 계약.
 */
interface PublishEventPort {
    /**
     * 스토어 생성 이벤트를 발행한다.
     *
     * @param event 스토어 생성 이벤트
     */
    fun publishStoreCreated(event: StoreCreatedEvent)

    /**
     * 스토어 정보 변경 이벤트를 발행한다.
     *
     * @param event 스토어 정보 변경 이벤트
     */
    fun publishStoreInfoUpdated(event: StoreInfoUpdatedEvent)

    /**
     * 스토어 삭제 이벤트를 발행한다.
     *
     * @param event 스토어 삭제 이벤트
     */
    fun publishStoreDeleted(event: StoreDeletedEvent)
}
