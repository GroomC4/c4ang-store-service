package com.groom.store.domain.port

import com.groom.ecommerce.store.domain.model.StoreAudit

/**
 * 스토어 감사 로그 저장을 위한 Outbound Port.
 * Domain이 외부 저장소에 요구하는 감사 로그 저장 계약.
 */
interface SaveStoreAuditPort {
    /**
     * 감사 로그를 저장한다.
     *
     * @param audit 저장할 감사 로그
     * @return 저장된 감사 로그
     */
    fun save(audit: StoreAudit): StoreAudit
}
