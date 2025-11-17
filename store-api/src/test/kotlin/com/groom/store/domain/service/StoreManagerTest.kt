package com.groom.store.domain.service

import com.groom.store.common.annotation.UnitTest
import com.groom.store.common.enums.StoreStatus
import com.groom.store.common.exception.StoreException
import com.groom.store.domain.model.Store
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

/**
 * StoreManager 도메인 서비스 단위 테스트
 *
 * 비즈니스 로직 분기와 상태별 처리를 검증합니다.
 */
@UnitTest
@DisplayName("StoreManager 단위 테스트")
class StoreManagerTest {
    private lateinit var storePolicy: StorePolicy
    private lateinit var storeManager: StoreManager

    @BeforeEach
    fun setUp() {
        storePolicy = mockk()
        storeManager = StoreManager(storePolicy)
    }

    @Test
    @DisplayName("정상 상태(REGISTERED)의 스토어 정보를 성공적으로 수정한다")
    fun testUpdateStoreInfo_Success() {
        // given
        val storeId = UUID.randomUUID()
        val ownerUserId = UUID.randomUUID()
        val store =
            Store(
                ownerUserId = ownerUserId,
                name = "원래 이름",
                description = "원래 설명",
                status = StoreStatus.REGISTERED,
            ).apply {
                this.id = storeId
            }

        every { storePolicy.checkStoreAccess(storeId, ownerUserId) } returns Unit

        // when
        val result =
            storeManager.updateStoreInfo(
                store = store,
                newName = "새로운 이름",
                newDescription = "새로운 설명",
                requestUserId = ownerUserId,
            )

        // then
        assertThat(result.updatedStore.name).isEqualTo("새로운 이름")
        assertThat(result.updatedStore.description).isEqualTo("새로운 설명")
        assertThat(result.event).isNotNull
        verify { storePolicy.checkStoreAccess(storeId, ownerUserId) }
    }

    @Test
    @DisplayName("숨김 상태(HIDDEN)의 스토어 정보를 성공적으로 수정한다")
    fun testUpdateStoreInfo_HiddenStatus_Success() {
        // given
        val storeId = UUID.randomUUID()
        val ownerUserId = UUID.randomUUID()
        val store =
            Store(
                ownerUserId = ownerUserId,
                name = "숨김 스토어",
                description = "숨겨진 상태",
                status = StoreStatus.REGISTERED,
            ).apply {
                this.id = storeId
            }

        every { storePolicy.checkStoreAccess(storeId, ownerUserId) } returns Unit

        // when
        val result =
            storeManager.updateStoreInfo(
                store = store,
                newName = "수정된 이름",
                newDescription = "수정된 설명",
                requestUserId = ownerUserId,
            )

        // then
        assertThat(result.updatedStore.name).isEqualTo("수정된 이름")
        assertThat(result.event).isNotNull
    }

    @Test
    @DisplayName("일시정지 상태(SUSPENDED)의 스토어 수정 시 예외가 발생한다")
    fun testUpdateStoreInfo_SuspendedStatus_ThrowsException() {
        // given
        val storeId = UUID.randomUUID()
        val ownerUserId = UUID.randomUUID()
        val store =
            Store(
                ownerUserId = ownerUserId,
                name = "정지된 스토어",
                status = StoreStatus.SUSPENDED,
            ).apply {
                this.id = storeId
            }

        every { storePolicy.checkStoreAccess(storeId, ownerUserId) } returns Unit

        // when & then
        val exception =
            assertThrows<StoreException.CannotUpdateSuspendedStore> {
                storeManager.updateStoreInfo(
                    store = store,
                    newName = "수정 시도",
                    newDescription = null,
                    requestUserId = ownerUserId,
                )
            }

        assertThat(exception.storeId).isEqualTo(storeId)
        verify { storePolicy.checkStoreAccess(storeId, ownerUserId) }
    }

    @Test
    @DisplayName("삭제된 상태(DELETED)의 스토어 수정 시 예외가 발생한다")
    fun testUpdateStoreInfo_DeletedStatus_ThrowsException() {
        // given
        val storeId = UUID.randomUUID()
        val ownerUserId = UUID.randomUUID()
        val store =
            Store(
                ownerUserId = ownerUserId,
                name = "삭제된 스토어",
                status = StoreStatus.DELETED,
            ).apply {
                this.id = storeId
            }

        every { storePolicy.checkStoreAccess(storeId, ownerUserId) } returns Unit

        // when & then
        val exception =
            assertThrows<StoreException.CannotUpdateDeletedStore> {
                storeManager.updateStoreInfo(
                    store = store,
                    newName = "수정 시도",
                    newDescription = null,
                    requestUserId = ownerUserId,
                )
            }

        assertThat(exception.storeId).isEqualTo(storeId)
        verify { storePolicy.checkStoreAccess(storeId, ownerUserId) }
    }

    @Test
    @DisplayName("소유자가 아닌 사용자가 스토어 수정 시도 시 예외가 발생한다")
    fun testUpdateStoreInfo_NonOwner_ThrowsException() {
        // given
        val storeId = UUID.randomUUID()
        val ownerUserId = UUID.randomUUID()
        val otherUserId = UUID.randomUUID()
        val store =
            Store(
                ownerUserId = ownerUserId,
                name = "타인의 스토어",
                status = StoreStatus.REGISTERED,
            ).apply {
                this.id = storeId
            }

        every { storePolicy.checkStoreAccess(storeId, otherUserId) } throws
            StoreException.StoreAccessDenied(storeId, otherUserId)

        // when & then
        val exception =
            assertThrows<StoreException.StoreAccessDenied> {
                storeManager.updateStoreInfo(
                    store = store,
                    newName = "수정 시도",
                    newDescription = null,
                    requestUserId = otherUserId,
                )
            }

        assertThat(exception.storeId).isEqualTo(storeId)
        assertThat(exception.userId).isEqualTo(otherUserId)
        verify { storePolicy.checkStoreAccess(storeId, otherUserId) }
    }

    @Test
    @DisplayName("스토어를 성공적으로 삭제한다")
    fun testDeleteStore_Success() {
        // given
        val storeId = UUID.randomUUID()
        val ownerUserId = UUID.randomUUID()
        val store =
            Store(
                ownerUserId = ownerUserId,
                name = "삭제할 스토어",
                status = StoreStatus.REGISTERED,
            ).apply {
                this.id = storeId
            }

        every { storePolicy.checkStoreAccess(storeId, ownerUserId) } returns Unit

        // when
        val result =
            storeManager.deleteStore(
                store = store,
                requestUserId = ownerUserId,
            )

        // then
        assertThat(result.deletedStore.status).isEqualTo(StoreStatus.DELETED)
        assertThat(result.deletedStore.deletedAt).isNotNull()
        assertThat(result.event).isNotNull
        verify { storePolicy.checkStoreAccess(storeId, ownerUserId) }
    }

    @Test
    @DisplayName("이미 삭제된 스토어 재삭제 시도 시 예외가 발생한다")
    fun testDeleteStore_AlreadyDeleted_ThrowsException() {
        // given
        val storeId = UUID.randomUUID()
        val ownerUserId = UUID.randomUUID()
        val store =
            Store(
                ownerUserId = ownerUserId,
                name = "이미 삭제된 스토어",
                status = StoreStatus.DELETED,
            ).apply {
                this.id = storeId
            }

        every { storePolicy.checkStoreAccess(storeId, ownerUserId) } returns Unit

        // when & then
        val exception =
            assertThrows<StoreException.StoreAlreadyDeleted> {
                storeManager.deleteStore(
                    store = store,
                    requestUserId = ownerUserId,
                )
            }

        assertThat(exception.storeId).isEqualTo(storeId)
        verify { storePolicy.checkStoreAccess(storeId, ownerUserId) }
    }

    @Test
    @DisplayName("소유자가 아닌 사용자가 스토어 삭제 시도 시 예외가 발생한다")
    fun testDeleteStore_NonOwner_ThrowsException() {
        // given
        val storeId = UUID.randomUUID()
        val ownerUserId = UUID.randomUUID()
        val otherUserId = UUID.randomUUID()
        val store =
            Store(
                ownerUserId = ownerUserId,
                name = "타인의 스토어",
                status = StoreStatus.REGISTERED,
            ).apply {
                this.id = storeId
            }

        every { storePolicy.checkStoreAccess(storeId, otherUserId) } throws
            StoreException.StoreAccessDenied(storeId, otherUserId)

        // when & then
        val exception =
            assertThrows<StoreException.StoreAccessDenied> {
                storeManager.deleteStore(
                    store = store,
                    requestUserId = otherUserId,
                )
            }

        assertThat(exception.storeId).isEqualTo(storeId)
        assertThat(exception.userId).isEqualTo(otherUserId)
        verify { storePolicy.checkStoreAccess(storeId, otherUserId) }
    }

    @Test
    @DisplayName("일시정지 상태의 스토어도 삭제할 수 있다")
    fun testDeleteStore_SuspendedStatus_Success() {
        // given
        val storeId = UUID.randomUUID()
        val ownerUserId = UUID.randomUUID()
        val store =
            Store(
                ownerUserId = ownerUserId,
                name = "정지된 스토어",
                status = StoreStatus.SUSPENDED,
            ).apply {
                this.id = storeId
            }

        every { storePolicy.checkStoreAccess(storeId, ownerUserId) } returns Unit

        // when
        val result =
            storeManager.deleteStore(
                store = store,
                requestUserId = ownerUserId,
            )

        // then
        assertThat(result.deletedStore.status).isEqualTo(StoreStatus.DELETED)
        assertThat(result.event).isNotNull
    }
}
