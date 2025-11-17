package com.groom.store.application.event

import com.groom.store.common.annotation.UnitTest
import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.event.StoreInfoSnapshot
import com.groom.store.domain.event.StoreInfoUpdatedEvent
import com.groom.store.domain.service.StoreAuditRecorder
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * StoreInfoUpdatedEventHandler 단위 테스트
 *
 * 이벤트 핸들러가 올바르게 호출되는지 검증합니다.
 */
@UnitTest
@DisplayName("StoreInfoUpdatedEventHandler 단위 테스트")
class StoreInfoUpdatedEventHandlerTest {
    private lateinit var storeAuditRecorder: StoreAuditRecorder
    private lateinit var eventHandler: StoreInfoUpdatedEventHandler

    @BeforeEach
    fun setUp() {
        storeAuditRecorder = mockk(relaxed = true)
        eventHandler = StoreInfoUpdatedEventHandler(storeAuditRecorder)
    }

    @Test
    @DisplayName("스토어 정보 수정 이벤트를 정상적으로 처리한다")
    fun testHandleStoreInfoUpdated() {
        // given
        val storeId = UUID.randomUUID()
        val ownerUserId = UUID.randomUUID()
        val before =
            StoreInfoSnapshot(
                name = "원래 이름",
                description = "원래 설명",
                status = StoreStatus.REGISTERED,
            )

        val after =
            StoreInfoSnapshot(
                name = "변경된 이름",
                description = "변경된 설명",
                status = StoreStatus.REGISTERED,
            )

        val event =
            StoreInfoUpdatedEvent(
                storeId = storeId,
                ownerUserId = ownerUserId,
                before = before,
                after = after,
            )

        // when
        eventHandler.handleStoreInfoUpdated(event)

        // then
        // 현재는 TODO 상태이므로 예외가 발생하지 않는지만 확인
        // 향후 Product 도메인 동기화 추가 시 검증 로직 추가 필요
    }

    @Test
    @DisplayName("스토어 이름만 변경된 이벤트를 처리한다")
    fun testHandleNameOnlyChange() {
        // given
        val storeId = UUID.randomUUID()
        val ownerUserId = UUID.randomUUID()
        val before =
            StoreInfoSnapshot(
                name = "원래 이름",
                description = "동일한 설명",
                status = StoreStatus.REGISTERED,
            )

        val after =
            StoreInfoSnapshot(
                name = "변경된 이름",
                description = "동일한 설명",
                status = StoreStatus.REGISTERED,
            )

        val event =
            StoreInfoUpdatedEvent(
                storeId = storeId,
                ownerUserId = ownerUserId,
                before = before,
                after = after,
            )

        // when
        eventHandler.handleStoreInfoUpdated(event)

        // then
        // 예외가 발생하지 않으면 성공
    }
}
