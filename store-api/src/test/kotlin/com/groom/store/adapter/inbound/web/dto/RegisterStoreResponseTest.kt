package com.groom.store.adapter.inbound.web.dto

import com.groom.store.application.dto.RegisterStoreResult
import com.groom.store.common.annotation.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

/**
 * RegisterStoreResponse DTO 변환 로직 단위 테스트
 *
 * Application Layer의 Result가 Response DTO로 올바르게 변환되는지 검증합니다.
 */
@UnitTest
@DisplayName("RegisterStoreResponse DTO 변환 테스트")
class RegisterStoreResponseTest {
    @Test
    @DisplayName("from() - 모든 필드가 있는 Result를 Response로 변환한다")
    fun testFrom_WithAllFields() {
        // given
        val storeId = UUID.randomUUID().toString()
        val ownerUserId = UUID.randomUUID().toString()
        val createdAt = LocalDateTime.now()
        val result =
            RegisterStoreResult(
                storeId = storeId,
                ownerUserId = ownerUserId,
                name = "테스트 스토어",
                description = "테스트 설명",
                status = "REGISTERED",
                createdAt = createdAt,
            )

        // when
        val response = RegisterStoreResponse.from(result)

        // then
        assertThat(response.storeId).isEqualTo(storeId)
        assertThat(response.name).isEqualTo("테스트 스토어")
        assertThat(response.description).isEqualTo("테스트 설명")
        assertThat(response.status).isEqualTo("REGISTERED")
        assertThat(response.createdAt).isEqualTo(createdAt)
    }

    @Test
    @DisplayName("from() - 설명이 null인 Result를 Response로 변환한다")
    fun testFrom_WithoutDescription() {
        // given
        val result =
            RegisterStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "테스트 스토어",
                description = null,
                status = "REGISTERED",
                createdAt = LocalDateTime.now(),
            )

        // when
        val response = RegisterStoreResponse.from(result)

        // then
        assertThat(response.name).isEqualTo("테스트 스토어")
        assertThat(response.description).isNull()
        assertThat(response.status).isEqualTo("REGISTERED")
    }

    @Test
    @DisplayName("from() - 빈 문자열 설명을 가진 Result를 Response로 변환한다")
    fun testFrom_WithEmptyDescription() {
        // given
        val result =
            RegisterStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "테스트 스토어",
                description = "",
                status = "REGISTERED",
                createdAt = LocalDateTime.now(),
            )

        // when
        val response = RegisterStoreResponse.from(result)

        // then
        assertThat(response.description).isEqualTo("")
    }

    @Test
    @DisplayName("from() - 여러 다른 상태의 Result를 Response로 변환한다")
    fun testFrom_DifferentStatuses() {
        // given
        val registeredResult =
            RegisterStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "등록된 스토어",
                description = null,
                status = "REGISTERED",
                createdAt = LocalDateTime.now(),
            )

        val suspendedResult =
            RegisterStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "정지된 스토어",
                description = null,
                status = "SUSPENDED",
                createdAt = LocalDateTime.now(),
            )

        // when
        val registeredResponse = RegisterStoreResponse.from(registeredResult)
        val suspendedResponse = RegisterStoreResponse.from(suspendedResult)

        // then
        assertThat(registeredResponse.status).isEqualTo("REGISTERED")
        assertThat(suspendedResponse.status).isEqualTo("SUSPENDED")
    }

    @Test
    @DisplayName("from() - 시간 정보가 정확하게 변환된다")
    fun testFrom_TimestampAccuracy() {
        // given
        val specificTime = LocalDateTime.of(2025, 11, 17, 10, 30, 45)
        val result =
            RegisterStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "테스트 스토어",
                description = "테스트",
                status = "REGISTERED",
                createdAt = specificTime,
            )

        // when
        val response = RegisterStoreResponse.from(result)

        // then
        assertThat(response.createdAt).isEqualTo(specificTime)
        assertThat(response.createdAt.year).isEqualTo(2025)
        assertThat(response.createdAt.monthValue).isEqualTo(11)
        assertThat(response.createdAt.dayOfMonth).isEqualTo(17)
        assertThat(response.createdAt.hour).isEqualTo(10)
        assertThat(response.createdAt.minute).isEqualTo(30)
        assertThat(response.createdAt.second).isEqualTo(45)
    }
}
