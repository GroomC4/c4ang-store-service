package com.groom.store.domain.service

import com.groom.ecommerce.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import java.util.Optional
import java.util.UUID

/**
 * Store 도메인의 조회 인터페이스.
 */
interface StoreReader {
    /**
     * 스토어 ID로 스토어를 조회한다.
     *
     * @param storeId 스토어 ID
     * @return 조회된 스토어
     */
    fun findById(storeId: UUID): Optional<Store>

    /**
     * 판매자 사용자 ID로 스토어를 조회한다.
     *
     * @param ownerUserId 판매자 사용자 ID
     * @return 조회된 스토어
     */
    fun findByOwnerUserId(ownerUserId: UUID): Optional<Store>

    /**
     * 스토어 상태로 스토어 목록을 조회한다.
     *
     * @param status 스토어 상태
     * @return 조회된 스토어 목록
     */
    fun findByStatus(status: StoreStatus): List<Store>

    /**
     * 스토어 이름으로 검색한다.
     *
     * @param name 검색할 스토어 이름
     * @return 조회된 스토어 목록
     */
    fun findByNameContaining(name: String): List<Store>

    /**
     * 판매자가 스토어를 보유하고 있는지 확인한다.
     *
     * @param ownerUserId 판매자 사용자 ID
     * @return 스토어 존재 여부
     */
    fun existsByOwnerUserId(ownerUserId: UUID): Boolean

    /**
     * 여러 스토어 ID로 스토어 목록을 조회한다 (Batch).
     *
     * @param storeIds 스토어 ID 목록
     * @return 조회된 스토어 목록
     */
    fun findAllById(storeIds: Iterable<UUID>): List<Store>
}
