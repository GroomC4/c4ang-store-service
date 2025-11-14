package com.groom.store.application.service

import com.groom.store.application.dto.DeleteStoreCommand
import com.groom.store.application.dto.DeleteStoreResult
import com.groom.store.common.exception.StoreException
import com.groom.store.domain.port.LoadStorePort
import com.groom.store.domain.port.PublishEventPort
import com.groom.store.domain.port.SaveStorePort
import com.groom.store.domain.service.StoreManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 스토어 삭제 애플리케이션 서비스.
 * 스토어 삭제 유스케이스를 처리한다.
 */
@Service
class DeleteStoreService(
    private val loadStorePort: LoadStorePort,
    private val saveStorePort: SaveStorePort,
    private val publishEventPort: PublishEventPort,
    private val storeManager: StoreManager,
) {
    /**
     * 스토어를 삭제한다 (소프트 삭제).
     *
     * @param command 스토어 삭제 커맨드
     * @return 스토어 삭제 결과
     * @throws StoreException.StoreNotFound 스토어를 찾을 수 없는 경우
     * @throws StoreException.StoreAccessDenied 스토어 소유자가 아닌 경우
     * @throws StoreException.StoreAlreadyDeleted 이미 삭제된 스토어인 경우
     */
    @Transactional
    fun delete(command: DeleteStoreCommand): DeleteStoreResult {
        // 스토어 조회
        val store =
            loadStorePort.loadById(command.storeId)
                ?: throw StoreException.StoreNotFound(command.storeId)

        // 도메인 서비스를 통한 스토어 삭제 (비즈니스 규칙 검증 포함, 불변 객체 패턴)
        val deleteResult = storeManager.deleteStore(store, command.userId)

        // 새 Store 인스턴스 저장 (불변 객체 패턴)
        val savedStore = saveStorePort.save(deleteResult.deletedStore)

        // 도메인 이벤트 발행
        publishEventPort.publishStoreDeleted(deleteResult.event)

        return DeleteStoreResult(
            storeId = savedStore.id.toString(),
            ownerUserId = savedStore.ownerUserId.toString(),
            name = savedStore.name,
            deletedAt = savedStore.deletedAt!!,
        )
    }
}
