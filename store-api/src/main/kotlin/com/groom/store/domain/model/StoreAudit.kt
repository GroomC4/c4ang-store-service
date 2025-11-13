package com.groom.store.domain.model

import com.groom.ecommerce.store.common.enums.StoreAuditEventType
import com.groom.ecommerce.store.common.enums.StoreStatus
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import java.time.LocalDateTime
import java.util.UUID

/**
 * StoreAudit 엔티티.
 * DDL: p_store_audit 테이블
 *
 * 스토어 등록/정지/삭제 등 상태 변화를 기록하는 감사 로그.
 */
@Entity
@Table(name = "p_store_audit")
class StoreAudit(
    @Column(nullable = false)
    val storeId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val eventType: StoreAuditEventType,
    @Enumerated(EnumType.STRING)
    @Column
    val statusSnapshot: StoreStatus? = null,
    @Column
    val changeSummary: String? = null,
    @Column
    val actorUserId: UUID? = null,
    @Column(nullable = false)
    val recordedAt: LocalDateTime = LocalDateTime.now(),
    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    val metadata: Map<String, Any>? = null,
) {
    @Id
    @Column(columnDefinition = "uuid", updatable = false)
    var id: UUID = UUID.randomUUID()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StoreAudit) return false
        if (id == null || other.id == null) return false
        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: System.identityHashCode(this)

    override fun toString(): String = "StoreAudit(id=$id, storeId=$storeId, eventType=$eventType, recordedAt=$recordedAt)"
}
