package com.groom.store.domain.service

import com.groom.store.domain.model.Store

/**
 * Store 도메인의 CUD 인터페이스.
 */
interface StoreWriter {
    fun save(store: Store): Store
}
