package com.groom.store.fixture

import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import com.groom.store.domain.model.StoreRating
import com.groom.store.domain.service.StoreFactory
import com.groom.store.domain.service.StorePolicy
import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * 단위 테스트용 StoreFactory 구현체
 * 빈으로 등록하지 않고, 필요한 테스트에서 생성자로 직접 주입하여 사용
 */
class NoOpsStoreAdapter(
    storePolicy: StorePolicy,
) : StoreFactory(storePolicy) {
    override fun createNewStore(
        ownerUserId: UUID,
        name: String,
        description: String?,
    ): Store {
        logger.info { "NoOpsStoreAdapter.createNewStore called (Test Mode)" }
        logger.debug { "ownerUserId=$ownerUserId, name=$name, description=$description" }

        val store =
            Store(
                ownerUserId = ownerUserId,
                name = name,
                description = description,
                status = StoreStatus.REGISTERED,
            )

        val storeRating =
            StoreRating(
                averageRating = BigDecimal.ZERO,
                reviewCount = 0,
                launchedAt = LocalDateTime.now(),
            ).apply {
                this.store = store
            }

        return store.apply { this.rating = storeRating }
    }
}
