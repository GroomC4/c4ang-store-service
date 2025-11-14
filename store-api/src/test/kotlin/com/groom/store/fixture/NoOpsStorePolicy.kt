package com.groom.store.fixture

import com.groom.store.domain.port.LoadStorePort
import com.groom.store.domain.service.StorePolicy
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * 테스트 환경 전용 StorePolicy 구현체
 */
@Component
@Profile("test")
@Primary
class NoOpsStorePolicy(
    loadStorePort: LoadStorePort,
) : StorePolicy(loadStorePort) {
    override fun checkStoreAlreadyExists(ownerUserId: UUID) {
        // No-op for testing - allows store creation without validation
        return
    }
}
