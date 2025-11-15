package com.groom.store.outbound.repository

import com.groom.store.domain.model.StoreRating
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface StoreRatingRepository : JpaRepository<StoreRating, UUID> {
    fun findByStore_Id(storeId: UUID): Optional<StoreRating>
}
