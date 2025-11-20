package com.groom.store.events.base

import java.time.LocalDateTime

/**
 * 도메인 이벤트 마커 인터페이스.
 * 모든 도메인 이벤트는 이 인터페이스를 구현해야 한다.
 *
 * 이 인터페이스는 이벤트 기반 아키텍처의 기반이 되며,
 * 도메인 계층과 애플리케이션 계층 간의 느슨한 결합을 가능하게 한다.
 */
interface DomainEvent {
    /**
     * 이벤트가 발생한 시각
     * 기본값으로 현재 시각을 반환한다.
     */
    val occurredAt: LocalDateTime
        get() = LocalDateTime.now()
}