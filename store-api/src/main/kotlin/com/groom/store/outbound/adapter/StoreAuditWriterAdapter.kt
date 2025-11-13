package com.groom.ecommerce.store.infrastructure.adapter

import com.groom.ecommerce.store.domain.model.StoreAudit
import com.groom.ecommerce.store.domain.port.StoreAuditWriter
import com.groom.ecommerce.store.infrastructure.repository.StoreAuditRepositoryImpl
import org.springframework.stereotype.Component

@Component
class StoreAuditWriterAdapter(
    private val storeAuditRepositoryImpl: StoreAuditRepositoryImpl,
) : StoreAuditWriter {
    override fun save(audit: StoreAudit): StoreAudit = storeAuditRepositoryImpl.save(audit)
}
