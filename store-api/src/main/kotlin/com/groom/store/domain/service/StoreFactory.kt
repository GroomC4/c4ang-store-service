package com.groom.store.domain.service

import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import com.groom.store.domain.model.StoreRating
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Store 애그리게이트의 복잡한 생성 로직을 담당하는 팩토리.
 * Store와 StoreRating의 일관성 있는 생성과 양방향 관계 설정을 보장한다.
 * 도메인 정책을 적용하여 유효하지 않은 스토어 생성을 방지한다.
 */
@Component
class StoreFactory(
    private val storePolicy: StorePolicy,
) {
    /**
     * 새로운 스토어를 생성한다.
     * Store와 StoreRating을 함께 생성하고 양방향 관계를 설정한다.
     * 생성 전 도메인 정책 검증을 수행한다.
     *
     * @param ownerUserId 스토어 소유자의 UUID
     * @param name 스토어명
     * @param description 스토어 설명 (선택)
     * @return 생성된 Store 엔티티 (StoreRating이 포함됨)
     * @throws com.groom.ecommerce.common.exception.StoreException.DuplicateStore 이미 스토어를 보유한 경우
     */
    fun createNewStore(
        ownerUserId: UUID,
        name: String,
        description: String?,
    ): Store {
        // 스토어 중복 확인 (한 사용자는 하나의 스토어만 보유 가능)
        storePolicy.checkStoreAlreadyExists(ownerUserId)
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
