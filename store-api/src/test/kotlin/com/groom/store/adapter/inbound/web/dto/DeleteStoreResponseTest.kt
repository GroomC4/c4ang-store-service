package com.groom.store.adapter.inbound.web.dto

import com.groom.store.application.dto.DeleteStoreResult
import com.groom.store.common.annotation.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

/**
 * DeleteStoreResponse DTO 변환 로직 단위 테스트
 *
 * Application Layer의 Result가 Response DTO로 올바르게 변환되는지 검증합니다.
 */
@UnitTest
@DisplayName("DeleteStoreResponse DTO 변환 테스트")
class DeleteStoreResponseTest {
    @Test
    @DisplayName("from() - 모든 필드가 있는 Result를 Response로 변환한다")
    fun testFrom_WithAllFields() {
        // given
        val storeId = UUID.randomUUID().toString()
        val ownerUserId = UUID.randomUUID().toString()
        val deletedAt = LocalDateTime.now()

        val result =
            DeleteStoreResult(
                storeId = storeId,
                ownerUserId = ownerUserId,
                name = "삭제된 스토어",
                deletedAt = deletedAt,
            )

        // when
        val response = DeleteStoreResponse.from(result)

        // then
        assertThat(response.storeId).isEqualTo(storeId)
        assertThat(response.ownerUserId).isEqualTo(ownerUserId)
        assertThat(response.name).isEqualTo("삭제된 스토어")
        assertThat(response.deletedAt).isEqualTo(deletedAt)
    }

    @Test
    @DisplayName("from() - 삭제 시간이 정확하게 변환된다")
    fun testFrom_DeletedAtAccuracy() {
        // given
        val specificTime = LocalDateTime.of(2025, 11, 17, 16, 45, 30)
        val result =
            DeleteStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "테스트 스토어",
                deletedAt = specificTime,
            )

        // when
        val response = DeleteStoreResponse.from(result)

        // then
        assertThat(response.deletedAt).isEqualTo(specificTime)
        assertThat(response.deletedAt.year).isEqualTo(2025)
        assertThat(response.deletedAt.monthValue).isEqualTo(11)
        assertThat(response.deletedAt.dayOfMonth).isEqualTo(17)
        assertThat(response.deletedAt.hour).isEqualTo(16)
        assertThat(response.deletedAt.minute).isEqualTo(45)
        assertThat(response.deletedAt.second).isEqualTo(30)
    }

    @Test
    @DisplayName("from() - 여러 다른 스토어의 삭제 Result를 각각 Response로 변환한다")
    fun testFrom_MultipleDeletedStores() {
        // given
        val result1 =
            DeleteStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "스토어 1",
                deletedAt = LocalDateTime.now().minusDays(1),
            )
        val result2 =
            DeleteStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "스토어 2",
                deletedAt = LocalDateTime.now(),
            )

        // when
        val response1 = DeleteStoreResponse.from(result1)
        val response2 = DeleteStoreResponse.from(result2)

        // then
        assertThat(response1.storeId).isNotEqualTo(response2.storeId)
        assertThat(response1.name).isEqualTo("스토어 1")
        assertThat(response2.name).isEqualTo("스토어 2")
        assertThat(response1.deletedAt).isBefore(response2.deletedAt)
    }

    @Test
    @DisplayName("from() - 같은 소유자의 여러 스토어 삭제 Result를 Response로 변환한다")
    fun testFrom_SameOwnerMultipleStores() {
        // given
        val ownerUserId = UUID.randomUUID().toString()
        val result1 =
            DeleteStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = ownerUserId,
                name = "스토어 A",
                deletedAt = LocalDateTime.now(),
            )
        val result2 =
            DeleteStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = ownerUserId,
                name = "스토어 B",
                deletedAt = LocalDateTime.now(),
            )

        // when
        val response1 = DeleteStoreResponse.from(result1)
        val response2 = DeleteStoreResponse.from(result2)

        // then
        assertThat(response1.ownerUserId).isEqualTo(ownerUserId)
        assertThat(response2.ownerUserId).isEqualTo(ownerUserId)
        assertThat(response1.storeId).isNotEqualTo(response2.storeId)
    }

    @Test
    @DisplayName("from() - 긴 이름을 가진 스토어의 삭제 Result를 Response로 변환한다")
    fun testFrom_LongStoreName() {
        // given
        val longName = "매우 긴 스토어 이름 ".repeat(10)
        val result =
            DeleteStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = longName,
                deletedAt = LocalDateTime.now(),
            )

        // when
        val response = DeleteStoreResponse.from(result)

        // then
        assertThat(response.name).isEqualTo(longName)
    }

    @Test
    @DisplayName("from() - 과거 시간의 삭제 Result를 Response로 변환한다")
    fun testFrom_PastDeletionTime() {
        // given
        val pastTime = LocalDateTime.now().minusMonths(6)
        val result =
            DeleteStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "오래 전 삭제된 스토어",
                deletedAt = pastTime,
            )

        // when
        val response = DeleteStoreResponse.from(result)

        // then
        assertThat(response.deletedAt).isEqualTo(pastTime)
        assertThat(response.deletedAt).isBefore(LocalDateTime.now())
    }
}
