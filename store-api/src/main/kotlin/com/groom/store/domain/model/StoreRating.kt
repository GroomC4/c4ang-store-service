package com.groom.store.domain.model

import com.groom.store.configuration.jpa.CreatedAndUpdatedAtAuditEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * StoreRating 엔티티.
 * DDL: p_store_rating 테이블 (Store와 1:1 관계)
 */
@Entity
@Table(name = "p_store_rating")
class StoreRating(
    @Column(nullable = false, precision = 3, scale = 2)
    val averageRating: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false)
    val reviewCount: Int = 0,
    @Column(nullable = false)
    val launchedAt: LocalDateTime,
    @Column
    val hiddenAt: LocalDateTime? = null,
    @Column
    val deletedAt: LocalDateTime? = null,
) : CreatedAndUpdatedAtAuditEntity() {
    @Id
    @Column(columnDefinition = "uuid", updatable = false)
    var id: UUID = UUID.randomUUID()

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, unique = true)
    var store: Store? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StoreRating) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "StoreRating(id=$id, averageRating=$averageRating, reviewCount=$reviewCount)"
}
