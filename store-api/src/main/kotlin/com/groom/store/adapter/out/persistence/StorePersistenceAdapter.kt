package com.groom.store.adapter.out.persistence

import com.groom.ecommerce.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import com.groom.store.domain.port.LoadStorePort
import com.groom.store.domain.port.SaveStorePort
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * 스토어 영속성 Adapter.
 * Domain Port를 구현하고, Spring Data JPA를 사용하여 실제 데이터 접근을 수행합니다.
 */
@Component
class StorePersistenceAdapter(
    private val storeJpaRepository: StoreJpaRepository,
) : LoadStorePort,
    SaveStorePort {
    override fun loadById(storeId: UUID): Store? = storeJpaRepository.findById(storeId).orElse(null)

    override fun loadByOwnerUserId(ownerUserId: UUID): Store? = storeJpaRepository.findByOwnerUserId(ownerUserId)

    override fun loadByStatus(status: StoreStatus): List<Store> = storeJpaRepository.findByStatus(status)

    override fun loadByNameContaining(name: String): List<Store> = storeJpaRepository.findByNameContaining(name)

    override fun existsByOwnerUserId(ownerUserId: UUID): Boolean = storeJpaRepository.existsByOwnerUserId(ownerUserId)

    override fun loadAllById(storeIds: Iterable<UUID>): List<Store> = storeJpaRepository.findAllById(storeIds)

    override fun save(store: Store): Store = storeJpaRepository.save(store)
}
