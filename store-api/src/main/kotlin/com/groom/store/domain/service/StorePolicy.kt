package com.groom.store.domain.service

import com.groom.ecommerce.store.common.enums.StoreStatus
import com.groom.store.common.exception.StoreException
import com.groom.store.domain.port.LoadStorePort
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Store 도메인의 비즈니스 규칙을 검증하는 정책 클래스.
 */
@Component
class StorePolicy(
    private val loadStorePort: LoadStorePort,
) {
    /**
     * 사용자가 이미 스토어를 보유하고 있는지 확인한다.
     * 한 사용자는 하나의 스토어만 보유할 수 있다.
     *
     * @param ownerUserId 사용자 UUID
     * @throws StoreException.DuplicateStore 이미 스토어를 보유한 경우
     */
    fun checkStoreAlreadyExists(ownerUserId: UUID) {
        if (loadStorePort.existsByOwnerUserId(ownerUserId)) {
            throw StoreException.DuplicateStore(ownerUserId)
        }
    }

    /**
     * 스토어를 삭제할 수 있는지 확인한다.
     *
     * @param storeId 스토어 UUID
     * @throws StoreException.StoreNotFound 스토어를 찾을 수 없는 경우
     * @throws StoreException.StoreAlreadyDeleted 이미 삭제된 스토어인 경우
     */
    fun checkStoreDeletable(storeId: UUID) {
        val store =
            loadStorePort.loadById(storeId)
                ?: throw StoreException.StoreNotFound(storeId)

        if (store.status == StoreStatus.DELETED) {
            throw StoreException.StoreAlreadyDeleted(storeId)
        }
    }

    /**
     * 사용자가 스토어에 접근할 수 있는지 확인한다.
     *
     * @param storeId 스토어 UUID
     * @param userId 사용자 UUID
     * @throws StoreException.StoreAccessDenied 스토어 소유자가 아닌 경우
     */
    fun checkStoreAccess(
        storeId: UUID,
        userId: UUID,
    ) {
        val store =
            loadStorePort.loadById(storeId)
                ?: throw StoreException.StoreNotFound(storeId)

        if (store.ownerUserId != userId) {
            throw StoreException.StoreAccessDenied(storeId, userId)
        }
    }

    /**
     * 스토어가 존재하는지 확인한다.
     *
     * @param storeId 스토어 UUID
     * @throws StoreException.StoreNotFound 스토어를 찾을 수 없는 경우
     */
    fun checkStoreExists(storeId: UUID) {
        loadStorePort.loadById(storeId)
            ?: throw StoreException.StoreNotFound(storeId)
    }
}
