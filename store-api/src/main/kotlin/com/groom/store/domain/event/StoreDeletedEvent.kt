package com.groom.store.domain.event

import com.groom.store.events.base.DomainEvent
import java.time.LocalDateTime
import java.util.UUID

/**
 * 스토어가 삭제되었을 때 발생하는 도메인 이벤트.
 *
 * @property storeId 삭제된 스토어 ID
 * @property ownerUserId 스토어 소유자 ID
 * @property storeName 스토어명
 * @property occurredAt 이벤트 발생 시각
 */
data class StoreDeletedEvent(
    val storeId: UUID,
    val ownerUserId: UUID,
    val storeName: String,
    override val occurredAt: LocalDateTime = LocalDateTime.now(),
) : DomainEvent
