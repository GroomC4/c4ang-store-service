package com.groom.store.outbound.repository

import com.groom.ecommerce.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface StoreRepositoryImpl : JpaRepository<Store, UUID> {
    fun findByOwnerUserId(ownerUserId: UUID): Optional<Store>

    fun findByStatus(status: StoreStatus): List<Store>

    fun findByNameContaining(name: String): List<Store>

    fun existsByOwnerUserId(ownerUserId: UUID): Boolean

    /**
     * 여러 Store를 한 번에 조회 (N+1 방지용).
     */
    fun findByIdIn(ids: Collection<UUID>): List<Store>
}
