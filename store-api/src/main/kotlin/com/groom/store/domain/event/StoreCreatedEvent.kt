package com.groom.store.domain.event

import com.groom.store.common.domain.DomainEvent
import java.time.LocalDateTime
import java.util.UUID

/**
 * 스토어가 생성되었을 때 발생하는 도메인 이벤트.
 *
 * @property storeId 생성된 스토어 ID
 * @property ownerUserId 스토어 소유자 ID
 * @property storeName 스토어명
 * @property description 스토어 설명
 * @property occurredAt 이벤트 발생 시각
 */
data class StoreCreatedEvent(
    val storeId: UUID,
    val ownerUserId: UUID,
    val storeName: String,
    val description: String?,
    override val occurredAt: LocalDateTime = LocalDateTime.now(),
) : DomainEvent
