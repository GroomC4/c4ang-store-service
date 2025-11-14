package com.groom.store.fixture

import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import com.groom.store.domain.model.StoreRating
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.apply
import kotlin.jvm.javaClass

/**
 * Store 엔티티 테스트 픽스처
 *
 * JPA Auditing으로 설정되는 createdAt/updatedAt 같은 필드를
 * 리플렉션으로 초기화하여 테스트용 Store 객체를 생성합니다.
 */
object StoreTestFixture {
    /**
     * 기본 Store 생성
     */
    fun createStore(
        id: UUID = UUID.randomUUID(),
        ownerUserId: UUID = UUID.randomUUID(),
        name: String = "Test Store",
        description: String? = "Test Store Description",
        status: StoreStatus = StoreStatus.REGISTERED,
        hiddenAt: LocalDateTime? = null,
        deletedAt: LocalDateTime? = null,
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now(),
    ): Store {
        val store =
            Store(
                ownerUserId = ownerUserId,
                name = name,
                description = description,
                status = status,
                hiddenAt = hiddenAt,
                deletedAt = deletedAt,
            )

        // 리플렉션으로 protected 필드 설정
        setField(store, "id", id)
        setField(store, "createdAt", createdAt)
        setField(store, "updatedAt", updatedAt)

        return store
    }

    /**
     * 정지된(SUSPENDED) Store 생성
     */
    fun createSuspendedStore(
        ownerUserId: UUID = UUID.randomUUID(),
        name: String = "Suspended Store",
        description: String? = "Suspended Store Description",
    ): Store =
        createStore(
            ownerUserId = ownerUserId,
            name = name,
            description = description,
            status = StoreStatus.SUSPENDED,
        )

    /**
     * 삭제된(DELETED) Store 생성
     */
    fun createDeletedStore(
        ownerUserId: UUID = UUID.randomUUID(),
        name: String = "Deleted Store",
    ): Store =
        createStore(
            ownerUserId = ownerUserId,
            name = name,
            status = StoreStatus.DELETED,
            deletedAt = LocalDateTime.now(),
        )

    /**
     * StoreRating과 함께 Store 생성
     */
    fun createStoreWithRating(
        id: UUID = UUID.randomUUID(),
        ownerUserId: UUID = UUID.randomUUID(),
        name: String = "Store with Rating",
        description: String? = "Store Description",
        status: StoreStatus = StoreStatus.REGISTERED,
        averageRating: BigDecimal = BigDecimal("4.5"),
        reviewCount: Int = 100,
        launchedAt: LocalDateTime = LocalDateTime.now(),
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now(),
    ): Store {
        val store =
            createStore(
                id = id,
                ownerUserId = ownerUserId,
                name = name,
                description = description,
                status = status,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        val storeRating =
            StoreRating(
                averageRating = averageRating,
                reviewCount = reviewCount,
                launchedAt = launchedAt,
            ).apply {
                this.store = store
            }

        store.rating = storeRating
        return store
    }

    /**
     * 리플렉션으로 필드 설정 (private/protected 필드 접근)
     */
    fun setField(
        target: Any,
        fieldName: String,
        value: Any?,
    ) {
        var clazz: Class<*>? = target.javaClass
        while (clazz != null) {
            try {
                val field = clazz.getDeclaredField(fieldName)
                field.isAccessible = true
                field.set(target, value)
                return
            } catch (e: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        throw NoSuchFieldException("Field $fieldName not found in ${target.javaClass}")
    }
}
