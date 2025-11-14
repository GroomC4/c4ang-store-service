package com.groom.store.domain.service

import com.groom.store.common.enums.StoreAuditEventType
import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.event.StoreCreatedEvent
import com.groom.store.domain.event.StoreDeletedEvent
import com.groom.store.domain.event.StoreInfoUpdatedEvent
import com.groom.store.domain.model.StoreAudit
import com.groom.store.domain.port.SaveStoreAuditPort
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

/**
 * 스토어 감사 로그 기록을 담당하는 도메인 서비스.
 * StoreAudit 생성 로직을 캡슐화하고 중복을 제거한다.
 *
 * 도메인 서비스 네이밍 규칙:
 * - Application 계층의 Service와 혼동을 방지하기 위해 'Service' postfix를 사용하지 않음
 * - 대신 책임을 나타내는 명확한 이름 사용 (예: Recorder, Policy, Factory, Manager)
 */
@Component
class StoreAuditRecorder(
    private val saveStoreAuditPort: SaveStoreAuditPort,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 스토어 생성 이벤트로부터 감사 로그를 생성하고 저장한다.
     *
     * @param event 스토어 생성 이벤트
     */
    fun recordStoreCreated(event: StoreCreatedEvent) {
        logger.info { "Recording store created: storeId=${event.storeId}, storeName=${event.storeName}" }

        val changeSummary = "스토어 '${event.storeName}' 생성됨"
        val metadata =
            mapOf(
                "storeName" to event.storeName,
                "description" to (event.description ?: ""),
                "createdBy" to event.ownerUserId.toString(),
            )

        val audit =
            StoreAudit(
                storeId = event.storeId,
                eventType = StoreAuditEventType.REGISTERED,
                statusSnapshot = StoreStatus.REGISTERED,
                changeSummary = changeSummary,
                actorUserId = event.ownerUserId,
                recordedAt = event.occurredAt,
                metadata = metadata,
            )

        saveStoreAuditPort.save(audit)
        logger.info { "StoreAudit saved: id=${audit.id}, storeId=${event.storeId}, eventType=REGISTERED" }
    }

    /**
     * 스토어 정보 수정 이벤트로부터 감사 로그를 생성하고 저장한다.
     *
     * @param event 스토어 정보 수정 이벤트
     */
    fun recordStoreInfoUpdated(event: StoreInfoUpdatedEvent) {
        logger.info {
            "Recording store info updated: storeId=${event.storeId}, " +
            "before=(name=${event.before.name}, status=${event.before.status}), " +
            "after=(name=${event.after.name}, status=${event.after.status})"
        }

        val changeSummary = buildInfoUpdateChangeSummary(event)
        val metadata = buildInfoUpdateMetadata(event)

        val audit =
            StoreAudit(
                storeId = event.storeId,
                eventType = StoreAuditEventType.INFO_UPDATED,
                statusSnapshot = if (event.isStatusChanged()) event.after.status else null,
                changeSummary = changeSummary,
                actorUserId = event.ownerUserId,
                recordedAt = event.occurredAt,
                metadata = metadata,
            )

        saveStoreAuditPort.save(audit)
        logger.info { "StoreAudit saved: id=${audit.id}, storeId=${event.storeId}, eventType=INFO_UPDATED" }
    }

    /**
     * 스토어 삭제 이벤트로부터 감사 로그를 생성하고 저장한다.
     *
     * @param event 스토어 삭제 이벤트
     */
    fun recordStoreDeleted(event: StoreDeletedEvent) {
        logger.info { "Recording store deleted: storeId=${event.storeId}, storeName=${event.storeName}" }

        val changeSummary = "스토어 '${event.storeName}' 삭제됨"
        val metadata =
            mapOf(
                "storeName" to event.storeName,
                "deletedBy" to event.ownerUserId.toString(),
            )

        val audit =
            StoreAudit(
                storeId = event.storeId,
                eventType = StoreAuditEventType.DELETED,
                statusSnapshot = StoreStatus.DELETED,
                changeSummary = changeSummary,
                actorUserId = event.ownerUserId,
                recordedAt = event.occurredAt,
                metadata = metadata,
            )

        saveStoreAuditPort.save(audit)
        logger.info { "StoreAudit saved: id=${audit.id}, storeId=${event.storeId}, eventType=DELETED" }
    }

    /**
     * 스토어 정보 수정 이벤트로부터 변경사항 요약을 생성한다.
     */
    private fun buildInfoUpdateChangeSummary(event: StoreInfoUpdatedEvent): String {
        val changes = mutableListOf<String>()

        if (event.isNameChanged()) {
            changes.add("이름: '${event.before.name}' → '${event.after.name}'")
        }

        if (event.isDescriptionChanged()) {
            val oldDesc = event.before.description ?: "없음"
            val newDesc = event.after.description ?: "없음"
            changes.add("설명: '$oldDesc' → '$newDesc'")
        }

        if (event.isStatusChanged()) {
            changes.add("상태: '${event.before.status}' → '${event.after.status}'")
        }

        return changes.joinToString(", ")
    }

    /**
     * 스토어 정보 수정 이벤트로부터 메타데이터를 생성한다.
     */
    private fun buildInfoUpdateMetadata(event: StoreInfoUpdatedEvent): Map<String, Any> =
        mapOf(
            "before" to mapOf(
                "name" to event.before.name,
                "description" to (event.before.description ?: ""),
                "status" to event.before.status.name,
            ),
            "after" to mapOf(
                "name" to event.after.name,
                "description" to (event.after.description ?: ""),
                "status" to event.after.status.name,
            ),
        )
}
