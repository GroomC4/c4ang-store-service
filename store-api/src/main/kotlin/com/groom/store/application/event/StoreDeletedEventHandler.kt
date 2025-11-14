package com.groom.store.application.event

import com.groom.store.domain.event.StoreDeletedEvent
import com.groom.store.domain.service.StoreAuditRecorder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * StoreDeletedEvent를 처리하는 이벤트 핸들러.
 * 스토어 삭제 이벤트를 감지하고 감사 로그(StoreAudit)를 생성한다.
 *
 * TransactionalEventListener를 사용하여 메인 트랜잭션이 커밋된 후
 * 별도의 독립적인 트랜잭션에서 감사 로그를 저장한다.
 */
@Component
class StoreDeletedEventHandler(
    private val storeAuditRecorder: StoreAuditRecorder,
) {
    /**
     * 스토어 삭제 이벤트를 처리한다.
     * 메인 트랜잭션 커밋 후에 실행되며, 별도의 새로운 트랜잭션에서 동작한다.
     *
     * @param event 스토어 삭제 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleStoreDeleted(event: StoreDeletedEvent) {
        storeAuditRecorder.recordStoreDeleted(event)
    }
}
