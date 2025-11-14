package com.groom.store.application.event

import com.groom.store.application.event.StoreCreatedEventHandler
import com.groom.store.common.annotation.UnitTest
import com.groom.store.domain.event.StoreCreatedEvent
import com.groom.store.domain.service.StoreAuditRecorder
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.LocalDateTime
import java.util.UUID

@UnitTest
class StoreCreatedEventHandlerTest :
    FunSpec({

        test("StoreCreatedEvent를 처리하면 StoreAuditRecorder가 호출된다") {
            // given
            val storeAuditRecorder = mockk<StoreAuditRecorder>()
            val handler = StoreCreatedEventHandler(storeAuditRecorder)

            val storeId = UUID.randomUUID()
            val ownerUserId = UUID.randomUUID()
            val event =
                StoreCreatedEvent(
                    storeId = storeId,
                    ownerUserId = ownerUserId,
                    storeName = "테크 스토어",
                    description = "최신 전자제품 판매",
                    occurredAt = LocalDateTime.now(),
                )

            every { storeAuditRecorder.recordStoreCreated(event) } just runs

            // when
            handler.handleStoreCreated(event)

            // then
            verify(exactly = 1) { storeAuditRecorder.recordStoreCreated(event) }
        }

        test("description이 null인 StoreCreatedEvent도 정상 처리된다") {
            // given
            val storeAuditRecorder = mockk<StoreAuditRecorder>()
            val handler = StoreCreatedEventHandler(storeAuditRecorder)

            val storeId = UUID.randomUUID()
            val ownerUserId = UUID.randomUUID()
            val event =
                StoreCreatedEvent(
                    storeId = storeId,
                    ownerUserId = ownerUserId,
                    storeName = "간단한 스토어",
                    description = null,
                    occurredAt = LocalDateTime.now(),
                )

            every { storeAuditRecorder.recordStoreCreated(event) } just runs

            // when
            handler.handleStoreCreated(event)

            // then
            verify(exactly = 1) { storeAuditRecorder.recordStoreCreated(event) }
        }
    })
