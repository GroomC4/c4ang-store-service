package com.groom.store.adapter.out.persistence

import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * Spring Data JPA Repository (Adapter 내부 기술 구현).
 * Port를 구현하는 Adapter가 이것을 사용합니다.
 */
interface StoreJpaRepository : JpaRepository<Store, UUID> {
    fun findByOwnerUserId(ownerUserId: UUID): Store?

    fun findByStatus(status: StoreStatus): List<Store>

    fun findByNameContaining(name: String): List<Store>

    fun existsByOwnerUserId(ownerUserId: UUID): Boolean

    fun findByIdIn(ids: Collection<UUID>): List<Store>
}
