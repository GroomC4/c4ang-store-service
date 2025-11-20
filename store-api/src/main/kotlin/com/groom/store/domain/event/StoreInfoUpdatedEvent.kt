package com.groom.store.domain.event

import com.groom.store.events.base.DomainEvent
import com.groom.store.common.enums.StoreStatus
import java.time.LocalDateTime
import java.util.UUID

/**
 * 스토어 정보 스냅샷.
 * 변경 전후 상태를 표현하기 위한 불변 객체.
 */
data class StoreInfoSnapshot(
    val name: String,
    val description: String?,
    val status: StoreStatus,
)

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
    val before: StoreInfoSnapshot,
    val after: StoreInfoSnapshot,
    override val occurredAt: LocalDateTime = LocalDateTime.now(),
) : DomainEvent {
    /**
     * 특정 필드가 변경되었는지 확인합니다.
     */
    fun isNameChanged(): Boolean = before.name != after.name

    fun isDescriptionChanged(): Boolean = before.description != after.description

    fun isStatusChanged(): Boolean = before.status != after.status
}
