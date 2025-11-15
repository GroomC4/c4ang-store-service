package com.groom.store.fixture

import com.groom.store.domain.port.LoadStorePort
import com.groom.store.domain.service.StorePolicy
import java.util.UUID

/**
 * 단위 테스트용 StorePolicy 구현체
 * 빈으로 등록하지 않고, 필요한 테스트에서 생성자로 직접 주입하여 사용
 */
class NoOpsStorePolicy(
    loadStorePort: LoadStorePort,
) : StorePolicy(loadStorePort) {
    override fun checkStoreAlreadyExists(ownerUserId: UUID) {
        // No-op for unit testing - allows store creation without validation
        return
    }
}
