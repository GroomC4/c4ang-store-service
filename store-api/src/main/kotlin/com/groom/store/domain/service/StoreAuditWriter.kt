package com.groom.store.domain.service

import com.groom.ecommerce.store.domain.model.StoreAudit

/**
 * Store 도메인의 감사 로그 저장 인터페이스.
 */
interface StoreAuditWriter {
    /**
     * 감사 로그를 저장한다.
     *
     * @param audit 저장할 감사 로그
     * @return 저장된 감사 로그
     */
    fun save(audit: StoreAudit): StoreAudit
}
