package com.groom.store.domain.event

import com.groom.ecommerce.common.domain.DomainEvent
import com.groom.store.common.domain.DomainEvent
import java.time.LocalDateTime
import java.util.UUID

/**
 * 스토어 정보가 수정되었을 때 발생하는 도메인 이벤트.
 *
 * 이 이벤트는 다음과 같은 경우에 활용될 수 있습니다:
 * - 감사 로그 기록
 * - 알림 발송 (스토어 구독자에게)
 * - 검색 인덱스 업데이트
 * - 캐시 무효화
 */
data class StoreInfoUpdatedEvent(
    val storeId: UUID,
    val ownerUserId: UUID,
    val oldName: String,
    val newName: String,
    val oldDescription: String?,
    val newDescription: String?,
    override val occurredAt: LocalDateTime = LocalDateTime.now(),
) : DomainEvent
