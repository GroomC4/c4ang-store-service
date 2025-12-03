package com.groom.store.domain.port

import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import java.util.UUID

/**
 * 스토어 조회를 위한 Outbound Port.
 * Domain이 외부 저장소에 요구하는 조회 계약.
 */
interface LoadStorePort {
    /**
     * 스토어 ID로 스토어를 조회한다.
     *
     * @param storeId 스토어 ID
     * @return 조회된 스토어 (없으면 null)
     */
    fun loadById(storeId: UUID): Store?

    /**
     * 판매자 사용자 ID로 스토어를 조회한다.
     *
     * @param ownerUserId 판매자 사용자 ID
     * @return 조회된 스토어 (없으면 null)
     */
    fun loadByOwnerUserId(ownerUserId: UUID): Store?

    /**
     * 스토어 상태로 스토어 목록을 조회한다.
     *
     * @param status 스토어 상태
     * @return 조회된 스토어 목록
     */
    fun loadByStatus(status: StoreStatus): List<Store>

    /**
     * 스토어 이름으로 검색한다.
     *
     * @param name 검색할 스토어 이름
     * @return 조회된 스토어 목록
     */
    fun loadByNameContaining(name: String): List<Store>

    /**
     * 판매자가 스토어를 보유하고 있는지 확인한다.
     *
     * @param ownerUserId 판매자 사용자 ID
     * @return 스토어 존재 여부
     */
    fun existsByOwnerUserId(ownerUserId: UUID): Boolean

    /**
     * 스토어 ID로 스토어가 존재하는지 확인한다.
     *
     * @param storeId 스토어 ID
     * @return 스토어 존재 여부
     */
    fun existsById(storeId: UUID): Boolean

    /**
     * 여러 스토어 ID로 스토어 목록을 조회한다 (Batch).
     *
     * @param storeIds 스토어 ID 목록
     * @return 조회된 스토어 목록
     */
    fun loadAllById(storeIds: Iterable<UUID>): List<Store>
}
