package com.groom.store.outbound.adapter

import com.groom.ecommerce.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import com.groom.store.domain.service.StoreReader
import com.groom.store.outbound.repository.StoreRepositoryImpl
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

/**
 * StoreReader 인터페이스의 구현체.
 * JPA Repository를 통해 스토어를 조회한다.
 */
@Component
class StoreReaderAdapter(
    private val storeRepository: StoreRepositoryImpl,
) : StoreReader {
    override fun findById(storeId: UUID): Optional<Store> = storeRepository.findById(storeId)

    override fun findByOwnerUserId(ownerUserId: UUID): Optional<Store> = storeRepository.findByOwnerUserId(ownerUserId)

    override fun findByStatus(status: StoreStatus): List<Store> = storeRepository.findByStatus(status)

    override fun findByNameContaining(name: String): List<Store> = storeRepository.findByNameContaining(name)

    override fun existsByOwnerUserId(ownerUserId: UUID): Boolean = storeRepository.existsByOwnerUserId(ownerUserId)

    override fun findAllById(storeIds: Iterable<UUID>): List<Store> = storeRepository.findAllById(storeIds)
}
