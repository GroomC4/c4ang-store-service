package com.groom.store.application.event

import com.groom.store.common.annotation.UnitTest
import com.groom.store.domain.event.StoreDeletedEvent
import com.groom.store.domain.service.StoreAuditRecorder
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

/**
 * StoreDeletedEventHandler 단위 테스트
 *
 * 이벤트 핸들러가 올바르게 호출되는지 검증합니다.
 */
@UnitTest
@DisplayName("StoreDeletedEventHandler 단위 테스트")
class StoreDeletedEventHandlerTest {
    private lateinit var storeAuditRecorder: StoreAuditRecorder
    private lateinit var eventHandler: StoreDeletedEventHandler

    @BeforeEach
    fun setUp() {
        storeAuditRecorder = mockk(relaxed = true)
        eventHandler = StoreDeletedEventHandler(storeAuditRecorder)
    }

    @Test
    @DisplayName("스토어 삭제 이벤트를 정상적으로 처리한다")
    fun testHandleStoreDeleted() {
        // given
        val event =
            StoreDeletedEvent(
                storeId = UUID.randomUUID(),
                ownerUserId = UUID.randomUUID(),
                storeName = "삭제된 스토어",
                occurredAt = LocalDateTime.now(),
            )

        // when
        eventHandler.handleStoreDeleted(event)

        // then
        // 현재는 TODO 상태이므로 예외가 발생하지 않는지만 확인
        // 향후 외부 시스템 연동 추가 시 검증 로직 추가 필요
    }

    @Test
    @DisplayName("여러 삭제 이벤트를 순차적으로 처리할 수 있다")
    fun testHandleMultipleDeleteEvents() {
        // given
        val event1 =
            StoreDeletedEvent(
                storeId = UUID.randomUUID(),
                ownerUserId = UUID.randomUUID(),
                storeName = "스토어 1",
                occurredAt = LocalDateTime.now(),
            )
        val event2 =
            StoreDeletedEvent(
                storeId = UUID.randomUUID(),
                ownerUserId = UUID.randomUUID(),
                storeName = "스토어 2",
                occurredAt = LocalDateTime.now(),
            )

        // when
        eventHandler.handleStoreDeleted(event1)
        eventHandler.handleStoreDeleted(event2)

        // then
        // 예외가 발생하지 않으면 성공
    }
}
