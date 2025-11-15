package com.groom.store.adapter.out.persistence

import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

/**
 * Store 엔티티에 대한 JPA Repository.
 * Spring Data JPA가 자동으로 구현체를 생성합니다.
 */
interface StoreRepository : JpaRepository<Store, UUID> {
    fun findByOwnerUserId(ownerUserId: UUID): Optional<Store>

    fun findByStatus(status: StoreStatus): List<Store>

    fun findByNameContaining(name: String): List<Store>

    fun existsByOwnerUserId(ownerUserId: UUID): Boolean

    fun findByIdIn(ids: Collection<UUID>): List<Store>
}
