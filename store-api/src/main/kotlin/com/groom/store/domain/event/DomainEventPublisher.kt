package com.groom.store.domain.event

/**
 * 도메인 이벤트를 발행하는 인터페이스.
 *
 * 이 인터페이스는 도메인 계층의 이벤트 발행을 추상화하여,
 * 구체적인 이벤트 인프라(Spring ApplicationEventPublisher, Kafka 등)로부터
 * 도메인 로직을 격리한다.
 *
 * 구현체는 다음과 같을 수 있다:
 * - Spring ApplicationEventPublisher 래퍼
 * - Kafka Producer 래퍼
 * - RabbitMQ Publisher 래퍼
 * - 테스트용 Mock Publisher
 */
interface DomainEventPublisher {
    /**
     * 도메인 이벤트를 발행한다.
     *
     * @param event 발행할 도메인 이벤트
     */
    fun publish(event: DomainEvent)

    /**
     * 여러 도메인 이벤트를 한 번에 발행한다.
     *
     * @param events 발행할 도메인 이벤트 목록
     */
    fun publishAll(events: Collection<DomainEvent>) {
        events.forEach { publish(it) }
    }
}
