package com.groom.store.application.event

import com.groom.store.domain.event.StoreCreatedEvent
import com.groom.store.domain.service.StoreAuditRecorder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * StoreCreatedEvent를 처리하는 이벤트 핸들러.
 * 스토어 생성 이벤트를 감지하고 감사 로그(StoreAudit)를 생성한다.
 *
 * TransactionalEventListener를 사용하여 메인 트랜잭션이 커밋된 후
 * 별도의 독립적인 트랜잭션에서 감사 로그를 저장한다.
 */
@Component
class StoreCreatedEventHandler(
    private val storeAuditRecorder: StoreAuditRecorder,
) {
    /**
     * 스토어 생성 이벤트를 처리한다.
     *
     * @param event 스토어 생성 이벤트
     *
     * NOTE: 감사 로그 기록은 이제 Application Service에서 직접 처리합니다.
     * 이벤트 핸들러는 나중에 다른 용도(예: 외부 시스템 연동)를 위해 유지합니다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleStoreCreated(event: StoreCreatedEvent) {
        // TODO: 다른 부가 작업이 필요한 경우 여기에 추가
    }
}
