package com.groom.store.application.service

import com.groom.store.application.dto.GetStoreByIdQuery
import com.groom.store.application.dto.GetStoreByOwnerIdQuery
import com.groom.store.application.dto.GetStoreInternalResult
import com.groom.store.application.port.inbound.GetStoreInternalUseCase
import com.groom.store.common.exception.StoreException
import com.groom.store.domain.port.LoadStorePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 스토어 내부 조회 서비스.
 *
 * 다른 마이크로서비스에서 스토어 정보를 조회할 때 사용하는 서비스.
 * GetStoreInternalUseCase 인터페이스를 구현한다.
 */
@Service
class GetStoreInternalService(
    private val loadStorePort: LoadStorePort,
) : GetStoreInternalUseCase {
    /**
     * 스토어 ID로 스토어 정보를 조회한다.
     *
     * @param query 조회 쿼리
     * @return 스토어 정보
     * @throws StoreException.StoreNotFound 스토어가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    override fun getStoreById(query: GetStoreByIdQuery): GetStoreInternalResult {
        val store =
            loadStorePort.loadById(query.storeId)
                ?: throw StoreException.StoreNotFound(query.storeId)

        return GetStoreInternalResult.from(store)
    }

    /**
     * 소유자 ID로 스토어 정보를 조회한다.
     *
     * @param query 조회 쿼리
     * @return 스토어 정보
     * @throws StoreException.StoreNotFound 스토어가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    override fun getStoreByOwnerId(query: GetStoreByOwnerIdQuery): GetStoreInternalResult {
        val store =
            loadStorePort.loadByOwnerUserId(query.ownerUserId)
                ?: throw StoreException.StoreNotFound(query.ownerUserId)

        return GetStoreInternalResult.from(store)
    }
}
