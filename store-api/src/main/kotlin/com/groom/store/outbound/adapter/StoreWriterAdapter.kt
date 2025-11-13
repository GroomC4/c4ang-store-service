package com.groom.store.outbound.adapter

import com.groom.store.domain.model.Store
import com.groom.store.domain.service.StoreWriter
import com.groom.store.outbound.repository.StoreRepositoryImpl
import org.springframework.stereotype.Component

/**
 * StoreReader 인터페이스의 구현체.
 * JPA Repository를 통해 스토어를 조회한다.
 */
@Component
class StoreWriterAdapter(
    private val storeRepository: StoreRepositoryImpl,
) : StoreWriter {
    override fun save(store: Store): Store = storeRepository.save<Store>(store)
}
