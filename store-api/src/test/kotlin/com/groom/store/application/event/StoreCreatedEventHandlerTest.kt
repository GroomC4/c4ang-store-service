package com.groom.store.application.event

import com.groom.store.common.annotation.UnitTest
import com.groom.store.domain.event.StoreCreatedEvent
import com.groom.store.domain.service.StoreAuditRecorder
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * StoreCreatedEventHandler 단위 테스트
 *
 * 이벤트 핸들러가 올바르게 호출되는지 검증합니다.
 */
@UnitTest
@DisplayName("StoreCreatedEventHandler 단위 테스트")
class StoreCreatedEventHandlerTest {
    private lateinit var storeAuditRecorder: StoreAuditRecorder
    private lateinit var eventHandler: StoreCreatedEventHandler

    @BeforeEach
    fun setUp() {
        storeAuditRecorder = mockk(relaxed = true)
        eventHandler = StoreCreatedEventHandler(storeAuditRecorder)
    }

    @Test
    @DisplayName("스토어 생성 이벤트를 정상적으로 처리한다")
    fun testHandleStoreCreated() {
        // given
        val event =
            StoreCreatedEvent(
                storeId = UUID.randomUUID(),
                ownerUserId = UUID.randomUUID(),
                storeName = "새로운 스토어",
                description = "테스트 설명",
            )

        // when
        eventHandler.handleStoreCreated(event)

        // then
        // 현재는 TODO 상태이므로 예외가 발생하지 않는지만 확인
        // 향후 부가 작업 추가 시 검증 로직 추가 필요
    }

    @Test
    @DisplayName("여러 이벤트를 순차적으로 처리할 수 있다")
    fun testHandleMultipleEvents() {
        // given
        val event1 =
            StoreCreatedEvent(
                storeId = UUID.randomUUID(),
                ownerUserId = UUID.randomUUID(),
                storeName = "스토어 1",
                description = null,
            )
        val event2 =
            StoreCreatedEvent(
                storeId = UUID.randomUUID(),
                ownerUserId = UUID.randomUUID(),
                storeName = "스토어 2",
                description = "설명 2",
            )

        // when
        eventHandler.handleStoreCreated(event1)
        eventHandler.handleStoreCreated(event2)

        // then
        // 예외가 발생하지 않으면 성공
    }
}
