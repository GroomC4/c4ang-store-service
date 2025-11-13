package com.groom.store.application.service

import com.groom.ecommerce.store.application.dto.UpdateStoreCommand
import com.groom.ecommerce.store.application.dto.UpdateStoreResult
import com.groom.store.common.domain.DomainEventPublisher
import com.groom.store.common.exception.StoreException
import com.groom.store.domain.service.StorePolicy
import com.groom.store.outbound.repository.StoreRepositoryImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 스토어 수정 유스케이스.
 * 스토어 소유자만 스토어 정보를 수정할 수 있다.
 */
@Service
class UpdateService(
    private val storeRepository: StoreRepositoryImpl,
    private val storePolicy: StorePolicy,
    private val domainEventPublisher: DomainEventPublisher,
) {
    /**
     * 스토어 정보를 수정한다.
     *
     * @param command 스토어 수정 명령
     * @return 수정된 스토어 정보
     * @throws StoreException.StoreNotFound 스토어를 찾을 수 없는 경우
     * @throws StoreException.StoreAccessDenied 스토어 소유자가 아닌 경우
     */
    @Transactional
    fun update(command: UpdateStoreCommand): UpdateStoreResult {
        // 스토어 접근 권한 검증 (Store 도메인 정책)
        storePolicy.checkStoreAccess(command.storeId, command.userId)

        // 스토어 조회
        val store =
            storeRepository
                .findById(command.storeId)
                .orElseThrow { StoreException.StoreNotFound(command.storeId) }

        // 스토어 정보 수정 (불변 객체이므로 새 인스턴스 반환)
        val updateResult =
            store.updateInfo(
                name = command.name,
                description = command.description,
            )

        // 새 Store 인스턴스 저장 (불변 객체 패턴)
        val savedStore = storeRepository.save(updateResult.updatedStore)

        // 도메인 이벤트 발행 (변경사항이 있는 경우에만)
        updateResult.event
            ?.let(domainEventPublisher::publish)

        return UpdateStoreResult(
            storeId = savedStore.id.toString(),
            ownerUserId = savedStore.ownerUserId.toString(),
            name = savedStore.name,
            description = savedStore.description,
            status = savedStore.status.name,
            updatedAt = savedStore.updatedAt!!,
        )
    }
}
