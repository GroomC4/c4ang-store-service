package com.groom.store.application.service

import com.groom.ecommerce.store.application.dto.GetStoreQuery
import com.groom.ecommerce.store.application.dto.GetStoreResult
import com.groom.store.common.exception.StoreException
import com.groom.store.domain.port.LoadStorePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 스토어 상세 조회 서비스.
 * 스토어의 상세 정보를 조회한다.
 */
@Service
class GetStoreService(
    private val loadStorePort: LoadStorePort,
) {
    /**
     * 스토어 상세 정보를 조회한다.
     *
     * @param query 조회 쿼리
     * @return 스토어 상세 정보
     * @throws StoreException.StoreNotFound 스토어가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    fun getStore(query: GetStoreQuery): GetStoreResult {
        val store = loadStorePort.loadById(query.storeId)
            ?: throw StoreException.StoreNotFound(query.storeId)

        return GetStoreResult.from(store)
    }

    /**
     * 스토어 상세 정보를 조회한다.
     *
     * @param query 조회 쿼리
     * @return 스토어 상세 정보
     * @throws StoreException.StoreNotFound 스토어가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    fun getMyStore(userId: UUID): GetStoreResult {
        val store = loadStorePort.loadByOwnerUserId(userId)
            ?: throw StoreException.StoreNotFound(userId)

        return GetStoreResult.from(store)
    }
}
