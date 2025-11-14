package com.groom.store.adapter.out.persistence

import com.groom.store.domain.model.StoreAudit
import com.groom.store.domain.port.SaveStoreAuditPort
import com.groom.store.outbound.repository.StoreAuditRepositoryImpl
import org.springframework.stereotype.Component

/**
 * 스토어 감사 로그 영속성 Adapter.
 * Domain Port를 구현하고, Spring Data JPA를 사용하여 감사 로그를 저장합니다.
 */
@Component
class StoreAuditPersistenceAdapter(
    private val storeAuditRepository: StoreAuditRepositoryImpl,
) : SaveStoreAuditPort {
    override fun save(audit: StoreAudit): StoreAudit = storeAuditRepository.save(audit)
}
