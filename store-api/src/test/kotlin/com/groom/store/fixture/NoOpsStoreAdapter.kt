package com.groom.store.fixture

import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import com.groom.store.domain.model.StoreRating
import com.groom.store.domain.service.StoreFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * 테스트 환경 전용 StoreFactory 구현체
 */
@Component
@Profile("test")
@Primary
class NoOpsStoreAdapter(
    storePolicy: NoOpsStorePolicy,
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
