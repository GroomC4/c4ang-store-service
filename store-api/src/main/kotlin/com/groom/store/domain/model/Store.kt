package com.groom.store.domain.model

import com.groom.store.common.enums.StoreStatus
import com.groom.store.common.exception.StoreException
import com.groom.store.configuration.jpa.CreatedAndUpdatedAtAuditEntity
import com.groom.store.domain.event.StoreCreatedEvent
import com.groom.store.domain.event.StoreDeletedEvent
import com.groom.store.domain.event.StoreInfoUpdatedEvent
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * Store 애그리게이트 루트.
 * DDL: p_store 테이블
 */
@Entity
@Table(name = "p_store")
class Store(
    // 향후 MSA 환경에서 사용자 관리를 별도 서비스로 분리할 경우를 대비해 UUID로 소유자 식별하고 관계매핑을 하지않는다
    @Column(nullable = false, unique = true)
    val ownerUserId: UUID,
    @Column(nullable = false)
    val name: String,
    @Column(columnDefinition = "text")
    val description: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: StoreStatus = StoreStatus.REGISTERED,
    @Column
    val hiddenAt: LocalDateTime? = null,
    @Column
    val deletedAt: LocalDateTime? = null,
) : CreatedAndUpdatedAtAuditEntity() {
    @Id
    @Column(columnDefinition = "uuid", updatable = false)
    var id: UUID = UUID.randomUUID()

    @OneToOne(mappedBy = "store", cascade = [CascadeType.ALL], orphanRemoval = true)
    var rating: StoreRating? = null

    /**
     * 스토어 생성 이벤트를 발행한다.
     * DB에 저장된 후 (id가 생성된 후) 호출되어야 한다.
     *
     * @return 발생한 도메인 이벤트
     * @throws IllegalStateException id가 null인 경우
     */
    fun publishCreatedEvent(): StoreCreatedEvent {
        checkNotNull(this.id) { "Store ID must not be null when publishing created event" }

        return StoreCreatedEvent(
            storeId = this.id,
            ownerUserId = this.ownerUserId,
            storeName = this.name,
            description = this.description,
        )
    }

    /**
     * 스토어 정보 수정 결과.
     *
     * @property updatedStore 수정된 새 Store 인스턴스
     * @property event 발생한 도메인 이벤트 (null이면 변경사항 없음)
     */
    data class UpdateInfoResult(
        val updatedStore: Store,
        val event: StoreInfoUpdatedEvent?,
    )

    /**
     * 스토어 정보를 수정한다.
     * 불변 객체이므로 새로운 Store 인스턴스를 생성하여 반환한다.
     *
     * @param name 스토어 이름
     * @param description 스토어 설명
     * @return 수정된 Store 인스턴스와 도메인 이벤트
     * @throws IllegalArgumentException 이름이 유효하지 않은 경우
     */
    fun updateInfo(
        name: String,
        description: String?,
    ): UpdateInfoResult {
        validateName(name)

        // 변경사항이 없으면 현재 인스턴스와 null 이벤트 반환
        if (this.name == name && this.description == description) {
            return UpdateInfoResult(this, null)
        }

        // 새로운 Store 인스턴스 생성
        val updatedStore =
            Store(
                ownerUserId = this.ownerUserId,
                name = name,
                description = description,
                status = this.status,
                hiddenAt = this.hiddenAt,
                deletedAt = this.deletedAt,
            ).apply {
                this.id = this@Store.id // 같은 ID 유지
                this.rating = this@Store.rating // 관계 유지

                // 감사 필드 복사 (리플렉션 사용)
                copyAuditFieldsFrom(this@Store)
            }

        // 도메인 이벤트 생성
        val event =
            StoreInfoUpdatedEvent(
                storeId = this.id,
                ownerUserId = this.ownerUserId,
                oldName = this.name,
                newName = name,
                oldDescription = this.description,
                newDescription = description,
            )

        return UpdateInfoResult(updatedStore, event)
    }

    /**
     * 스토어 삭제 결과.
     *
     * @property deletedStore 삭제된 새 Store 인스턴스
     * @property event 발생한 도메인 이벤트
     */
    data class DeleteResult(
        val deletedStore: Store,
        val event: StoreDeletedEvent,
    )

    /**
     * 스토어를 삭제한다 (소프트 삭제).
     * 불변 객체이므로 새로운 Store 인스턴스를 생성하여 반환한다.
     *
     * @return 삭제된 Store 인스턴스와 도메인 이벤트
     * @throws StoreException.StoreAlreadyDeleted 이미 삭제된 스토어인 경우
     */
    fun delete(): DeleteResult {
        if (this.status == StoreStatus.DELETED) {
            throw StoreException.StoreAlreadyDeleted(this.id)
        }

        // 새로운 Store 인스턴스 생성 (DELETED 상태)
        val deletedStore =
            Store(
                ownerUserId = this.ownerUserId,
                name = this.name,
                description = this.description,
                status = StoreStatus.DELETED,
                hiddenAt = this.hiddenAt,
                deletedAt = LocalDateTime.now(),
            ).apply {
                this.id = this@Store.id // 같은 ID 유지
                this.rating = this@Store.rating // 관계 유지

                // 감사 필드 복사 (리플렉션 사용)
                copyAuditFieldsFrom(this@Store)
            }

        // 도메인 이벤트 생성
        val event =
            StoreDeletedEvent(
                storeId = this.id,
                ownerUserId = this.ownerUserId,
                storeName = this.name,
            )

        return DeleteResult(deletedStore, event)
    }

    /**
     * 원본 Store의 감사 필드를 현재 인스턴스로 복사한다.
     * 리플렉션을 사용하여 protected setter를 우회한다.
     */
    private fun copyAuditFieldsFrom(original: Store) {
        try {
            val createdAtField =
                CreatedAndUpdatedAtAuditEntity::class.java
                    .getDeclaredField("createdAt")
                    .apply { isAccessible = true }

            val updatedAtField =
                CreatedAndUpdatedAtAuditEntity::class.java
                    .getDeclaredField("updatedAt")
                    .apply { isAccessible = true }

            createdAtField.set(this, original.createdAt)
            updatedAtField.set(this, original.updatedAt)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to copy audit fields", e)
        }
    }

    /**
     * 스토어 이름의 유효성을 검증한다.
     */
    private fun validateName(name: String) {
        require(name.isNotBlank()) { "스토어 이름은 비어있을 수 없습니다" }
        require(name.length <= 100) { "스토어 이름은 100자를 초과할 수 없습니다" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Store) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Store(id=$id, name=$name, ownerUserId=$ownerUserId, status=$status)"
}
