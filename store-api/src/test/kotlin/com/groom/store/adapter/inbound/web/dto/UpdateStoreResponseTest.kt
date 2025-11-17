package com.groom.store.adapter.inbound.web.dto

import com.groom.store.application.dto.UpdateStoreResult
import com.groom.store.common.annotation.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

/**
 * UpdateStoreResponse DTO 변환 로직 단위 테스트
 *
 * Application Layer의 Result가 Response DTO로 올바르게 변환되는지 검증합니다.
 */
@UnitTest
@DisplayName("UpdateStoreResponse DTO 변환 테스트")
class UpdateStoreResponseTest {
    @Test
    @DisplayName("from() - 모든 필드가 있는 Result를 Response로 변환한다")
    fun testFrom_WithAllFields() {
        // given
        val storeId = UUID.randomUUID().toString()
        val updatedAt = LocalDateTime.now()
        val result =
            UpdateStoreResult(
                storeId = storeId,
                ownerUserId = UUID.randomUUID().toString(),
                name = "수정된 스토어",
                description = "수정된 설명",
                status = "REGISTERED",
                updatedAt = updatedAt,
            )

        // when
        val response = UpdateStoreResponse.from(result)

        // then
        assertThat(response.storeId).isEqualTo(storeId)
        assertThat(response.name).isEqualTo("수정된 스토어")
        assertThat(response.description).isEqualTo("수정된 설명")
        assertThat(response.status).isEqualTo("REGISTERED")
        assertThat(response.updatedAt).isEqualTo(updatedAt)
    }

    @Test
    @DisplayName("from() - 설명이 null인 Result를 Response로 변환한다")
    fun testFrom_WithoutDescription() {
        // given
        val result =
            UpdateStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "수정된 스토어",
                description = null,
                status = "REGISTERED",
                updatedAt = LocalDateTime.now(),
            )

        // when
        val response = UpdateStoreResponse.from(result)

        // then
        assertThat(response.name).isEqualTo("수정된 스토어")
        assertThat(response.description).isNull()
    }

    @Test
    @DisplayName("from() - 이름만 변경된 Result를 Response로 변환한다")
    fun testFrom_OnlyNameChanged() {
        // given
        val result =
            UpdateStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "새로운 이름",
                description = null,
                status = "REGISTERED",
                updatedAt = LocalDateTime.now(),
            )

        // when
        val response = UpdateStoreResponse.from(result)

        // then
        assertThat(response.name).isEqualTo("새로운 이름")
        assertThat(response.description).isNull()
        assertThat(response.status).isEqualTo("REGISTERED")
    }

    @Test
    @DisplayName("from() - 빈 문자열 설명을 가진 Result를 Response로 변환한다")
    fun testFrom_WithEmptyDescription() {
        // given
        val result =
            UpdateStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "수정된 스토어",
                description = "",
                status = "REGISTERED",
                updatedAt = LocalDateTime.now(),
            )

        // when
        val response = UpdateStoreResponse.from(result)

        // then
        assertThat(response.description).isEqualTo("")
    }

    @Test
    @DisplayName("from() - 수정 시간이 정확하게 변환된다")
    fun testFrom_UpdatedAtAccuracy() {
        // given
        val specificTime = LocalDateTime.of(2025, 11, 17, 14, 20, 30)
        val result =
            UpdateStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "수정된 스토어",
                description = "수정된 설명",
                status = "REGISTERED",
                updatedAt = specificTime,
            )

        // when
        val response = UpdateStoreResponse.from(result)

        // then
        assertThat(response.updatedAt).isEqualTo(specificTime)
        assertThat(response.updatedAt.year).isEqualTo(2025)
        assertThat(response.updatedAt.monthValue).isEqualTo(11)
        assertThat(response.updatedAt.dayOfMonth).isEqualTo(17)
        assertThat(response.updatedAt.hour).isEqualTo(14)
        assertThat(response.updatedAt.minute).isEqualTo(20)
    }

    @Test
    @DisplayName("from() - 여러 다른 Result를 각각 Response로 변환한다")
    fun testFrom_MultipleResults() {
        // given
        val result1 =
            UpdateStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "스토어 1",
                description = "설명 1",
                status = "REGISTERED",
                updatedAt = LocalDateTime.now(),
            )
        val result2 =
            UpdateStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "스토어 2",
                description = "설명 2",
                status = "SUSPENDED",
                updatedAt = LocalDateTime.now(),
            )

        // when
        val response1 = UpdateStoreResponse.from(result1)
        val response2 = UpdateStoreResponse.from(result2)

        // then
        assertThat(response1.name).isEqualTo("스토어 1")
        assertThat(response2.name).isEqualTo("스토어 2")
        assertThat(response1.status).isEqualTo("REGISTERED")
        assertThat(response2.status).isEqualTo("SUSPENDED")
    }
}
