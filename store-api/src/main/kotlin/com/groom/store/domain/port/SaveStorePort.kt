package com.groom.store.domain.port

import com.groom.store.domain.model.Store

/**
 * 스토어 저장을 위한 Outbound Port.
 * Domain이 외부 저장소에 요구하는 저장 계약.
 */
interface SaveStorePort {
    /**
     * 스토어를 저장한다.
     *
     * @param store 저장할 스토어
     * @return 저장된 스토어
     */
    fun save(store: Store): Store
}
