package com.groom.ecommerce.store.infrastructure.repository

import com.groom.ecommerce.store.domain.model.StoreAudit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface StoreAuditRepositoryImpl : JpaRepository<StoreAudit, UUID> {
    /**
     * 특정 스토어의 감사 로그를 시간 역순으로 조회
     */
    fun findByStoreIdOrderByRecordedAtDesc(storeId: UUID): List<StoreAudit>
}
