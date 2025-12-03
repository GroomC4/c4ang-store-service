package com.groom.store.application.port.inbound

import com.groom.store.application.dto.CheckStoreExistsQuery
import com.groom.store.application.dto.CheckStoreExistsResult
import com.groom.store.application.dto.GetStoreByIdQuery
import com.groom.store.application.dto.GetStoreByOwnerIdQuery
import com.groom.store.application.dto.GetStoreForOrderQuery
import com.groom.store.application.dto.GetStoreForOrderResult
import com.groom.store.application.dto.GetStoreInternalResult

/**
 * 스토어 내부 조회 Use Case (Inbound Port).
 *
 * 다른 마이크로서비스에서 스토어 정보를 조회할 때 사용하는 인터페이스.
 * Istio 서비스 메시 내부에서만 접근 가능하며, 인증 헤더 검증을 수행하지 않습니다.
 */
interface GetStoreInternalUseCase {
    /**
     * 스토어 ID로 스토어 정보를 조회한다.
     *
     * @param query 조회 쿼리
     * @return 스토어 정보
     * @throws StoreException.StoreNotFound 스토어가 존재하지 않는 경우
     */
    fun getStoreById(query: GetStoreByIdQuery): GetStoreInternalResult

    /**
     * 소유자 ID로 스토어 정보를 조회한다.
     *
     * @param query 조회 쿼리
     * @return 스토어 정보
     * @throws StoreException.StoreNotFound 스토어가 존재하지 않는 경우
     */
    fun getStoreByOwnerId(query: GetStoreByOwnerIdQuery): GetStoreInternalResult

    /**
     * 스토어 존재 여부를 확인한다.
     *
     * @param query 조회 쿼리
     * @return 스토어 존재 여부
     */
    fun checkStoreExists(query: CheckStoreExistsQuery): CheckStoreExistsResult

    /**
     * Order Service용 스토어 정보를 조회한다.
     *
     * Order Service에서 주문 생성 시 필요한 최소한의 스토어 정보만 반환.
     *
     * @param query 조회 쿼리
     * @return 스토어 정보 (id, name, status)
     * @throws StoreException.StoreNotFound 스토어가 존재하지 않는 경우
     */
    fun getStoreForOrder(query: GetStoreForOrderQuery): GetStoreForOrderResult
}
