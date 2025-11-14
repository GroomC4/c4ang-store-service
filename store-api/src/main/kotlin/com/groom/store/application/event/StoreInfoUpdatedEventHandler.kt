package com.groom.store.application.event

import com.groom.store.domain.event.StoreInfoUpdatedEvent
import com.groom.store.domain.service.StoreAuditRecorder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * StoreInfoUpdatedEvent를 처리하는 이벤트 핸들러.
 *
 * 처리 내용:
 * 1. 감사 로그(StoreAudit) 생성
 * 2. 스토어 이름이 변경된 경우 p_product의 비정규화 컬럼(store_name) 일괄 업데이트
 *
 * TransactionalEventListener를 사용하여 메인 트랜잭션이 커밋된 후
 * 별도의 독립적인 트랜잭션에서 동작한다.
 */
@Component
class StoreInfoUpdatedEventHandler(
    private val storeAuditRecorder: StoreAuditRecorder,
    private val storeEnventPublisher: StoreEventPublisher,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 스토어 정보 수정 이벤트를 처리한다.
     * 메인 트랜잭션 커밋 후에 실행되며, 별도의 새로운 트랜잭션에서 동작한다.
     *
     * @param event 스토어 정보 수정 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleStoreInfoUpdated(event: StoreInfoUpdatedEvent) {
        // 1. 감사 로그 기록
        storeAuditRecorder.recordStoreInfoUpdated(event)

        // 2. 스토어 이름이 변경된 경우 비정규화 컬럼 업데이트
        if (event.oldName != event.newName) {
            updateProductStoreName(event)
        }
    }
}
