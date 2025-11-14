package com.groom.store.domain.service

import com.groom.ecommerce.common.annotation.UnitTest
import com.groom.store.common.enums.StoreAuditEventType
import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.event.StoreCreatedEvent
import com.groom.store.domain.event.StoreDeletedEvent
import com.groom.store.domain.event.StoreInfoUpdatedEvent
import com.groom.store.domain.model.StoreAudit
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.UUID

@UnitTest
class StoreAuditRecorderTest :
    FunSpec({

        test("StoreCreatedEvent를 처리하면 REGISTERED 타입의 감사 로그가 생성된다") {
            // given
            val storeAuditWriter = mockk<StoreAuditWriter>()
            val recorder = StoreAuditRecorder(storeAuditWriter)

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

            every { storeAuditWriter.save(any()) } answers { firstArg() }

            // when
            recorder.recordStoreCreated(event)

            // then
            verify(exactly = 1) {
                storeAuditWriter.save(
                    match<StoreAudit> {
                        it.storeId == storeId &&
                            it.eventType == StoreAuditEventType.REGISTERED &&
                            it.statusSnapshot == StoreStatus.REGISTERED &&
                            it.actorUserId == ownerUserId &&
                            it.changeSummary == "스토어 '테크 스토어' 생성됨" &&
                            it.metadata?.get("storeName") == "테크 스토어" &&
                            it.metadata?.get("description") == "최신 전자제품 판매" &&
                            it.metadata?.get("createdBy") == ownerUserId.toString()
                    },
                )
            }
        }

        test("description이 null인 StoreCreatedEvent도 감사 로그가 생성된다") {
            // given
            val storeAuditWriter = mockk<StoreAuditWriter>()
            val recorder = StoreAuditRecorder(storeAuditWriter)

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

            every { storeAuditWriter.save(any()) } answers { firstArg() }

            // when
            recorder.recordStoreCreated(event)

            // then
            verify(exactly = 1) {
                storeAuditWriter.save(
                    match<StoreAudit> {
                        it.storeId == storeId &&
                            it.eventType == StoreAuditEventType.REGISTERED &&
                            it.statusSnapshot == StoreStatus.REGISTERED &&
                            it.actorUserId == ownerUserId &&
                            it.changeSummary == "스토어 '간단한 스토어' 생성됨" &&
                            it.metadata?.get("storeName") == "간단한 스토어" &&
                            it.metadata?.get("description") == "" &&
                            it.metadata?.get("createdBy") == ownerUserId.toString()
                    },
                )
            }
        }

        test("StoreInfoUpdatedEvent를 처리하면 INFO_UPDATED 타입의 감사 로그가 생성된다") {
            // given
            val storeAuditWriter = mockk<StoreAuditWriter>()
            val recorder = StoreAuditRecorder(storeAuditWriter)

            val storeId = UUID.randomUUID()
            val ownerUserId = UUID.randomUUID()
            val event =
                StoreInfoUpdatedEvent(
                    storeId = storeId,
                    ownerUserId = ownerUserId,
                    oldName = "기존 스토어",
                    newName = "새 스토어",
                    oldDescription = "기존 설명",
                    newDescription = "새 설명",
                    occurredAt = LocalDateTime.now(),
                )

            every { storeAuditWriter.save(any()) } answers { firstArg() }

            // when
            recorder.recordStoreInfoUpdated(event)

            // then
            verify(exactly = 1) {
                storeAuditWriter.save(
                    match<StoreAudit> {
                        it.storeId == storeId &&
                            it.eventType == StoreAuditEventType.INFO_UPDATED &&
                            it.statusSnapshot == null &&
                            it.actorUserId == ownerUserId &&
                            it.changeSummary?.contains("이름: '기존 스토어' → '새 스토어'") == true &&
                            it.changeSummary?.contains("설명: '기존 설명' → '새 설명'") == true &&
                            it.metadata?.get("oldName") == "기존 스토어" &&
                            it.metadata?.get("newName") == "새 스토어" &&
                            it.metadata?.get("oldDescription") == "기존 설명" &&
                            it.metadata?.get("newDescription") == "새 설명"
                    },
                )
            }
        }

        test("이름만 변경된 경우 changeSummary에 이름 변경만 포함된다") {
            // given
            val storeAuditWriter = mockk<StoreAuditWriter>()
            val recorder = StoreAuditRecorder(storeAuditWriter)

            val storeId = UUID.randomUUID()
            val ownerUserId = UUID.randomUUID()
            val event =
                StoreInfoUpdatedEvent(
                    storeId = storeId,
                    ownerUserId = ownerUserId,
                    oldName = "기존 스토어",
                    newName = "새 스토어",
                    oldDescription = "동일한 설명",
                    newDescription = "동일한 설명",
                    occurredAt = LocalDateTime.now(),
                )

            every { storeAuditWriter.save(any()) } answers { firstArg() }

            // when
            recorder.recordStoreInfoUpdated(event)

            // then
            verify(exactly = 1) {
                storeAuditWriter.save(
                    match<StoreAudit> {
                        it.changeSummary == "이름: '기존 스토어' → '새 스토어'"
                    },
                )
            }
        }

        test("StoreDeletedEvent를 처리하면 DELETED 타입의 감사 로그가 생성된다") {
            // given
            val storeAuditWriter = mockk<StoreAuditWriter>()
            val recorder = StoreAuditRecorder(storeAuditWriter)

            val storeId = UUID.randomUUID()
            val ownerUserId = UUID.randomUUID()
            val event =
                StoreDeletedEvent(
                    storeId = storeId,
                    ownerUserId = ownerUserId,
                    storeName = "삭제될 스토어",
                    occurredAt = LocalDateTime.now(),
                )

            every { storeAuditWriter.save(any()) } answers { firstArg() }

            // when
            recorder.recordStoreDeleted(event)

            // then
            verify(exactly = 1) {
                storeAuditWriter.save(
                    match<StoreAudit> {
                        it.storeId == storeId &&
                            it.eventType == StoreAuditEventType.DELETED &&
                            it.statusSnapshot == StoreStatus.DELETED &&
                            it.actorUserId == ownerUserId &&
                            it.changeSummary == "스토어 '삭제될 스토어' 삭제됨" &&
                            it.metadata?.get("storeName") == "삭제될 스토어" &&
                            it.metadata?.get("deletedBy") == ownerUserId.toString()
                    },
                )
            }
        }
    })
