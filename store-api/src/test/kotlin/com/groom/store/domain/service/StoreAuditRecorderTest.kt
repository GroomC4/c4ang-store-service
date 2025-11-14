package com.groom.store.domain.service

import com.groom.store.common.annotation.UnitTest
import com.groom.store.common.enums.StoreAuditEventType
import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.event.StoreCreatedEvent
import com.groom.store.domain.event.StoreDeletedEvent
import com.groom.store.domain.event.StoreInfoSnapshot
import com.groom.store.domain.event.StoreInfoUpdatedEvent
import com.groom.store.domain.model.StoreAudit
import com.groom.store.domain.port.SaveStoreAuditPort
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
            val saveStoreAuditPort = mockk<SaveStoreAuditPort>()
            val recorder = StoreAuditRecorder(saveStoreAuditPort)

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

            every { saveStoreAuditPort.save(any()) } answers { firstArg() }

            // when
            recorder.recordStoreCreated(event)

            // then
            verify(exactly = 1) {
                saveStoreAuditPort.save(
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
            val saveStoreAuditPort = mockk<SaveStoreAuditPort>()
            val recorder = StoreAuditRecorder(saveStoreAuditPort)

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

            every { saveStoreAuditPort.save(any()) } answers { firstArg() }

            // when
            recorder.recordStoreCreated(event)

            // then
            verify(exactly = 1) {
                saveStoreAuditPort.save(
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
            val saveStoreAuditPort = mockk<SaveStoreAuditPort>()
            val recorder = StoreAuditRecorder(saveStoreAuditPort)

            val storeId = UUID.randomUUID()
            val ownerUserId = UUID.randomUUID()
            val event =
                StoreInfoUpdatedEvent(
                    storeId = storeId,
                    ownerUserId = ownerUserId,
                    before =
                        StoreInfoSnapshot(
                            name = "기존 스토어",
                            description = "기존 설명",
                            status = StoreStatus.REGISTERED,
                        ),
                    after =
                        StoreInfoSnapshot(
                            name = "새 스토어",
                            description = "새 설명",
                            status = StoreStatus.REGISTERED,
                        ),
                    occurredAt = LocalDateTime.now(),
                )

            every { saveStoreAuditPort.save(any()) } answers { firstArg() }

            // when
            recorder.recordStoreInfoUpdated(event)

            // then
            verify(exactly = 1) {
                saveStoreAuditPort.save(
                    match<StoreAudit> {
                        it.storeId == storeId &&
                            it.eventType == StoreAuditEventType.INFO_UPDATED &&
                            it.statusSnapshot == null &&
                            it.actorUserId == ownerUserId &&
                            it.changeSummary?.contains("이름: '기존 스토어' → '새 스토어'") == true &&
                            it.changeSummary?.contains("설명: '기존 설명' → '새 설명'") == true
                    },
                )
            }
        }

        test("이름만 변경된 경우 changeSummary에 이름 변경만 포함된다") {
            // given
            val saveStoreAuditPort = mockk<SaveStoreAuditPort>()
            val recorder = StoreAuditRecorder(saveStoreAuditPort)

            val storeId = UUID.randomUUID()
            val ownerUserId = UUID.randomUUID()
            val event =
                StoreInfoUpdatedEvent(
                    storeId = storeId,
                    ownerUserId = ownerUserId,
                    before =
                        StoreInfoSnapshot(
                            name = "기존 스토어",
                            description = "동일한 설명",
                            status = StoreStatus.REGISTERED,
                        ),
                    after =
                        StoreInfoSnapshot(
                            name = "새 스토어",
                            description = "동일한 설명",
                            status = StoreStatus.REGISTERED,
                        ),
                    occurredAt = LocalDateTime.now(),
                )

            every { saveStoreAuditPort.save(any()) } answers { firstArg() }

            // when
            recorder.recordStoreInfoUpdated(event)

            // then
            verify(exactly = 1) {
                saveStoreAuditPort.save(
                    match<StoreAudit> {
                        it.changeSummary == "이름: '기존 스토어' → '새 스토어'"
                    },
                )
            }
        }

        test("상태가 변경된 경우 statusSnapshot이 설정되고 changeSummary에 포함된다") {
            // given
            val saveStoreAuditPort = mockk<SaveStoreAuditPort>()
            val recorder = StoreAuditRecorder(saveStoreAuditPort)

            val storeId = UUID.randomUUID()
            val ownerUserId = UUID.randomUUID()
            val event =
                StoreInfoUpdatedEvent(
                    storeId = storeId,
                    ownerUserId = ownerUserId,
                    before =
                        StoreInfoSnapshot(
                            name = "테스트 스토어",
                            description = "설명",
                            status = StoreStatus.REGISTERED,
                        ),
                    after =
                        StoreInfoSnapshot(
                            name = "테스트 스토어",
                            description = "설명",
                            status = StoreStatus.SUSPENDED,
                        ),
                    occurredAt = LocalDateTime.now(),
                )

            every { saveStoreAuditPort.save(any()) } answers { firstArg() }

            // when
            recorder.recordStoreInfoUpdated(event)

            // then
            verify(exactly = 1) {
                saveStoreAuditPort.save(
                    match<StoreAudit> {
                        it.statusSnapshot == StoreStatus.SUSPENDED &&
                            it.changeSummary == "상태: 'REGISTERED' → 'SUSPENDED'"
                    },
                )
            }
        }

        test("StoreDeletedEvent를 처리하면 DELETED 타입의 감사 로그가 생성된다") {
            // given
            val saveStoreAuditPort = mockk<SaveStoreAuditPort>()
            val recorder = StoreAuditRecorder(saveStoreAuditPort)

            val storeId = UUID.randomUUID()
            val ownerUserId = UUID.randomUUID()
            val event =
                StoreDeletedEvent(
                    storeId = storeId,
                    ownerUserId = ownerUserId,
                    storeName = "삭제될 스토어",
                    occurredAt = LocalDateTime.now(),
                )

            every { saveStoreAuditPort.save(any()) } answers { firstArg() }

            // when
            recorder.recordStoreDeleted(event)

            // then
            verify(exactly = 1) {
                saveStoreAuditPort.save(
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
