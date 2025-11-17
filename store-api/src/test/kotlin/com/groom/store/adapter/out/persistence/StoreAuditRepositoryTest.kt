package com.groom.store.adapter.out.persistence

import com.groom.store.common.enums.StoreAuditEventType
import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.model.StoreAudit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID

/**
 * StoreAuditRepository JPA 레이어 테스트
 *
 * 실제 데이터베이스(Testcontainers)를 사용하여 JPA Repository의 CRUD 동작을 검증합니다.
 */
@DisplayName("StoreAuditRepository 테스트")
class StoreAuditRepositoryTest : BaseRepositoryTest() {
    @Autowired
    private lateinit var storeAuditRepository: StoreAuditRepository

    @Test
    @DisplayName("감사 로그를 저장하고 ID로 조회할 수 있다")
    fun testSaveAndFindById() {
        // given
        val storeId = UUID.randomUUID()
        val actorUserId = UUID.randomUUID()
        val audit =
            StoreAudit(
                storeId = storeId,
                eventType = StoreAuditEventType.REGISTERED,
                statusSnapshot = StoreStatus.REGISTERED,
                changeSummary = "스토어가 생성되었습니다",
                actorUserId = actorUserId,
                recordedAt = LocalDateTime.now(),
                metadata = mapOf("storeName" to "테스트 스토어"),
            )

        // when
        val savedAudit = storeAuditRepository.save(audit)
        val foundAudit = storeAuditRepository.findById(savedAudit.id)

        // then
        assertThat(foundAudit).isPresent
        assertThat(foundAudit.get().id).isEqualTo(savedAudit.id)
        assertThat(foundAudit.get().storeId).isEqualTo(storeId)
        assertThat(foundAudit.get().eventType).isEqualTo(StoreAuditEventType.REGISTERED)
        assertThat(foundAudit.get().statusSnapshot).isEqualTo(StoreStatus.REGISTERED)
        assertThat(foundAudit.get().changeSummary).isEqualTo("스토어가 생성되었습니다")
        assertThat(foundAudit.get().actorUserId).isEqualTo(actorUserId)
        assertThat(foundAudit.get().metadata).containsEntry("storeName", "테스트 스토어")
    }

    @Test
    @DisplayName("스토어 ID로 감사 로그를 시간 역순으로 조회할 수 있다")
    fun testFindByStoreIdOrderByRecordedAtDesc() {
        // given
        val storeId = UUID.randomUUID()
        val actorUserId = UUID.randomUUID()

        // 시간순으로 3개의 감사 로그 생성
        val audit1 =
            StoreAudit(
                storeId = storeId,
                eventType = StoreAuditEventType.REGISTERED,
                statusSnapshot = StoreStatus.REGISTERED,
                changeSummary = "스토어 생성",
                actorUserId = actorUserId,
                recordedAt = LocalDateTime.now().minusHours(2),
            )
        val audit2 =
            StoreAudit(
                storeId = storeId,
                eventType = StoreAuditEventType.INFO_UPDATED,
                statusSnapshot = StoreStatus.REGISTERED,
                changeSummary = "스토어 정보 수정",
                actorUserId = actorUserId,
                recordedAt = LocalDateTime.now().minusHours(1),
            )
        val audit3 =
            StoreAudit(
                storeId = storeId,
                eventType = StoreAuditEventType.DELETED,
                statusSnapshot = StoreStatus.DELETED,
                changeSummary = "스토어 삭제",
                actorUserId = actorUserId,
                recordedAt = LocalDateTime.now(),
            )

        storeAuditRepository.save(audit1)
        storeAuditRepository.save(audit2)
        storeAuditRepository.save(audit3)

        // when
        val audits = storeAuditRepository.findByStoreIdOrderByRecordedAtDesc(storeId)

        // then
        assertThat(audits).hasSize(3)
        // 시간 역순이므로 가장 최근 것이 먼저 나와야 함
        assertThat(audits[0].eventType).isEqualTo(StoreAuditEventType.DELETED)
        assertThat(audits[1].eventType).isEqualTo(StoreAuditEventType.INFO_UPDATED)
        assertThat(audits[2].eventType).isEqualTo(StoreAuditEventType.REGISTERED)
    }

    @Test
    @DisplayName("존재하지 않는 스토어 ID로 조회 시 빈 리스트를 반환한다")
    fun testFindByNonExistentStoreId() {
        // given
        val nonExistentStoreId = UUID.randomUUID()

        // when
        val audits = storeAuditRepository.findByStoreIdOrderByRecordedAtDesc(nonExistentStoreId)

        // then
        assertThat(audits).isEmpty()
    }

    @Test
    @DisplayName("여러 스토어의 감사 로그를 독립적으로 조회할 수 있다")
    fun testFindByMultipleStores() {
        // given
        val store1Id = UUID.randomUUID()
        val store2Id = UUID.randomUUID()
        val actorUserId = UUID.randomUUID()

        storeAuditRepository.save(
            StoreAudit(
                storeId = store1Id,
                eventType = StoreAuditEventType.REGISTERED,
                changeSummary = "스토어 1 생성",
                actorUserId = actorUserId,
            ),
        )
        storeAuditRepository.save(
            StoreAudit(
                storeId = store1Id,
                eventType = StoreAuditEventType.INFO_UPDATED,
                changeSummary = "스토어 1 수정",
                actorUserId = actorUserId,
            ),
        )
        storeAuditRepository.save(
            StoreAudit(
                storeId = store2Id,
                eventType = StoreAuditEventType.REGISTERED,
                changeSummary = "스토어 2 생성",
                actorUserId = actorUserId,
            ),
        )

        // when
        val store1Audits = storeAuditRepository.findByStoreIdOrderByRecordedAtDesc(store1Id)
        val store2Audits = storeAuditRepository.findByStoreIdOrderByRecordedAtDesc(store2Id)

        // then
        assertThat(store1Audits).hasSize(2)
        assertThat(store1Audits.map { it.changeSummary })
            .containsExactly("스토어 1 수정", "스토어 1 생성")

        assertThat(store2Audits).hasSize(1)
        assertThat(store2Audits[0].changeSummary).isEqualTo("스토어 2 생성")
    }

    @Test
    @DisplayName("메타데이터가 null인 감사 로그를 저장할 수 있다")
    fun testSaveAuditWithoutMetadata() {
        // given
        val audit =
            StoreAudit(
                storeId = UUID.randomUUID(),
                eventType = StoreAuditEventType.REGISTERED,
                statusSnapshot = StoreStatus.REGISTERED,
                actorUserId = UUID.randomUUID(),
                metadata = null,
            )

        // when
        val savedAudit = storeAuditRepository.save(audit)

        // then
        val foundAudit = storeAuditRepository.findById(savedAudit.id)
        assertThat(foundAudit).isPresent
        assertThat(foundAudit.get().metadata).isNull()
    }

    @Test
    @DisplayName("actorUserId가 null인 감사 로그를 저장할 수 있다 (시스템 이벤트)")
    fun testSaveSystemAudit() {
        // given
        val audit =
            StoreAudit(
                storeId = UUID.randomUUID(),
                eventType = StoreAuditEventType.SUSPENDED,
                statusSnapshot = StoreStatus.SUSPENDED,
                changeSummary = "시스템에 의한 자동 정지",
                actorUserId = null,
            )

        // when
        val savedAudit = storeAuditRepository.save(audit)

        // then
        val foundAudit = storeAuditRepository.findById(savedAudit.id)
        assertThat(foundAudit).isPresent
        assertThat(foundAudit.get().actorUserId).isNull()
        assertThat(foundAudit.get().changeSummary).isEqualTo("시스템에 의한 자동 정지")
    }

    @Test
    @DisplayName("복잡한 메타데이터를 포함한 감사 로그를 저장하고 조회할 수 있다")
    fun testSaveAuditWithComplexMetadata() {
        // given
        val metadata =
            mapOf(
                "previousName" to "이전 이름",
                "newName" to "새 이름",
                "previousDescription" to "이전 설명",
                "newDescription" to "새 설명",
                "changeCount" to 2,
            )

        val audit =
            StoreAudit(
                storeId = UUID.randomUUID(),
                eventType = StoreAuditEventType.INFO_UPDATED,
                changeSummary = "스토어 정보 대량 수정",
                actorUserId = UUID.randomUUID(),
                metadata = metadata,
            )

        // when
        val savedAudit = storeAuditRepository.save(audit)

        // then
        val foundAudit = storeAuditRepository.findById(savedAudit.id)
        assertThat(foundAudit).isPresent
        assertThat(foundAudit.get().metadata).containsAllEntriesOf(metadata)
    }

    @Test
    @DisplayName("감사 로그를 삭제할 수 있다")
    fun testDeleteAudit() {
        // given
        val audit =
            StoreAudit(
                storeId = UUID.randomUUID(),
                eventType = StoreAuditEventType.REGISTERED,
                actorUserId = UUID.randomUUID(),
            )
        val savedAudit = storeAuditRepository.save(audit)
        val auditId = savedAudit.id

        // when
        storeAuditRepository.delete(savedAudit)

        // then
        val foundAudit = storeAuditRepository.findById(auditId)
        assertThat(foundAudit).isEmpty
    }
}
