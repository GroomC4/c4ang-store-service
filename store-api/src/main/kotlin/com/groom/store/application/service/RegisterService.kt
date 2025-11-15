package com.groom.store.application.service

import com.groom.store.application.dto.RegisterStoreCommand
import com.groom.store.application.dto.RegisterStoreResult
import com.groom.store.domain.port.PublishEventPort
import com.groom.store.domain.port.SaveStorePort
import com.groom.store.domain.service.SellerPolicy
import com.groom.store.domain.service.StoreAuditRecorder
import com.groom.store.domain.service.StoreFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 스토어 등록 유스케이스.
 * 인증된 판매자가 스토어를 등록할 수 있다.
 */
@Service
class RegisterService(
    private val sellerPolicy: SellerPolicy,
    private val storeFactory: StoreFactory,
    private val saveStorePort: SaveStorePort,
    private val publishEventPort: PublishEventPort,
    private val storeAuditRecorder: StoreAuditRecorder,
) {
    /**
     * 스토어를 등록한다.
     *
     * @param command 스토어 등록 명령
     * @return 등록된 스토어 정보
     * @throws com.groom.store.common.exception.UserException.InsufficientPermission OWNER 역할이 아닌 경우
     * @throws com.groom.store.common.exception.StoreException.DuplicateStore 이미 스토어를 보유한 경우
     */
    @Transactional(readOnly = false)
    fun register(command: RegisterStoreCommand): RegisterStoreResult {
        // DB 기반 OWNER 역할 검증 (User 도메인 정책)
        sellerPolicy.checkOwnerRole(command.ownerUserId)

        // 스토어 생성 (Factory 내부에서 Store 도메인 정책 검증 수행)
        val newStore =
            storeFactory
                .createNewStore(
                    ownerUserId = command.ownerUserId,
                    name = command.name,
                    description = command.description,
                ).let { saveStorePort.save(it) }

        // 스토어 생성 이벤트 발행 및 감사 로그 기록
        val event = newStore.publishCreatedEvent()
        storeAuditRecorder.recordStoreCreated(event)
        publishEventPort.publishStoreCreated(event)

        return RegisterStoreResult(
            storeId = newStore.id.toString(),
            ownerUserId = newStore.ownerUserId.toString(),
            name = newStore.name,
            description = newStore.description,
            status = newStore.status.name,
            createdAt = newStore.createdAt!!,
        )
    }
}
