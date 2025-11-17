package com.groom.store.adapter.inbound.web.dto

import com.groom.store.application.dto.GetStoreResult
import com.groom.store.common.annotation.UnitTest
import com.groom.store.common.enums.StoreStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * GetStoreResponse DTO 변환 로직 단위 테스트
 *
 * Application Layer의 Result가 Response DTO로 올바르게 변환되는지 검증합니다.
 */
@UnitTest
@DisplayName("GetStoreResponse DTO 변환 테스트")
class GetStoreResponseTest {
    @Test
    @DisplayName("from() - 모든 필드가 있는 Result를 Response로 변환한다")
    fun testFrom_WithAllFields() {
        // given
        val storeId = UUID.randomUUID().toString()
        val ownerUserId = UUID.randomUUID().toString()
        val createdAt = LocalDateTime.now().minusDays(10)
        val updatedAt = LocalDateTime.now().minusDays(5)
        val launchedAt = LocalDateTime.now().minusDays(7)

        val result =
            GetStoreResult(
                storeId = storeId,
                ownerUserId = ownerUserId,
                name = "테스트 스토어",
                description = "테스트 설명",
                status = StoreStatus.REGISTERED,
                averageRating = BigDecimal("4.5"),
                reviewCount = 120,
                launchedAt = launchedAt,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        // when
        val response = GetStoreResponse.from(result)

        // then
        assertThat(response.storeId).isEqualTo(storeId)
        assertThat(response.ownerUserId).isEqualTo(ownerUserId)
        assertThat(response.name).isEqualTo("테스트 스토어")
        assertThat(response.description).isEqualTo("테스트 설명")
        assertThat(response.status).isEqualTo(StoreStatus.REGISTERED)
        assertThat(response.averageRating).isEqualTo(BigDecimal("4.5"))
        assertThat(response.reviewCount).isEqualTo(120)
        assertThat(response.launchedAt).isEqualTo(launchedAt)
        assertThat(response.createdAt).isEqualTo(createdAt)
        assertThat(response.updatedAt).isEqualTo(updatedAt)
    }

    @Test
    @DisplayName("from() - 설명이 null인 Result를 Response로 변환한다")
    fun testFrom_WithoutDescription() {
        // given
        val result =
            GetStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "테스트 스토어",
                description = null,
                status = StoreStatus.REGISTERED,
                averageRating = BigDecimal("4.0"),
                reviewCount = 50,
                launchedAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        // when
        val response = GetStoreResponse.from(result)

        // then
        assertThat(response.description).isNull()
    }

    @Test
    @DisplayName("from() - 평점 정보가 null인 Result를 Response로 변환한다")
    fun testFrom_WithoutRating() {
        // given
        val result =
            GetStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "신규 스토어",
                description = "아직 리뷰가 없는 스토어",
                status = StoreStatus.REGISTERED,
                averageRating = null,
                reviewCount = 0,
                launchedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        // when
        val response = GetStoreResponse.from(result)

        // then
        assertThat(response.averageRating).isNull()
        assertThat(response.reviewCount).isEqualTo(0)
        assertThat(response.launchedAt).isNull()
    }

    @Test
    @DisplayName("from() - 다양한 상태의 스토어를 Response로 변환한다")
    fun testFrom_DifferentStatuses() {
        // given
        val registeredResult =
            GetStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "등록된 스토어",
                description = null,
                status = StoreStatus.REGISTERED,
                averageRating = null,
                reviewCount = 0,
                launchedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        val suspendedResult =
            GetStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "정지된 스토어",
                description = null,
                status = StoreStatus.SUSPENDED,
                averageRating = BigDecimal("3.0"),
                reviewCount = 10,
                launchedAt = LocalDateTime.now().minusMonths(1),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        val deletedResult =
            GetStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "삭제된 스토어",
                description = null,
                status = StoreStatus.DELETED,
                averageRating = null,
                reviewCount = 0,
                launchedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        // when
        val registeredResponse = GetStoreResponse.from(registeredResult)
        val suspendedResponse = GetStoreResponse.from(suspendedResult)
        val deletedResponse = GetStoreResponse.from(deletedResult)

        // then
        assertThat(registeredResponse.status).isEqualTo(StoreStatus.REGISTERED)
        assertThat(suspendedResponse.status).isEqualTo(StoreStatus.SUSPENDED)
        assertThat(deletedResponse.status).isEqualTo(StoreStatus.DELETED)
    }

    @Test
    @DisplayName("from() - 높은 평점과 많은 리뷰를 가진 Result를 Response로 변환한다")
    fun testFrom_WithHighRatingAndManyReviews() {
        // given
        val result =
            GetStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "인기 스토어",
                description = "많은 리뷰가 있는 스토어",
                status = StoreStatus.REGISTERED,
                averageRating = BigDecimal("4.9"),
                reviewCount = 9999,
                launchedAt = LocalDateTime.now().minusYears(1),
                createdAt = LocalDateTime.now().minusYears(1),
                updatedAt = LocalDateTime.now(),
            )

        // when
        val response = GetStoreResponse.from(result)

        // then
        assertThat(response.averageRating).isEqualTo(BigDecimal("4.9"))
        assertThat(response.reviewCount).isEqualTo(9999)
    }

    @Test
    @DisplayName("from() - 시간 정보가 정확하게 변환된다")
    fun testFrom_TimestampAccuracy() {
        // given
        val createdTime = LocalDateTime.of(2025, 1, 1, 9, 0, 0)
        val updatedTime = LocalDateTime.of(2025, 11, 17, 15, 30, 45)
        val launchedTime = LocalDateTime.of(2025, 1, 15, 10, 0, 0)

        val result =
            GetStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "테스트 스토어",
                description = "시간 테스트",
                status = StoreStatus.REGISTERED,
                averageRating = BigDecimal("4.0"),
                reviewCount = 10,
                launchedAt = launchedTime,
                createdAt = createdTime,
                updatedAt = updatedTime,
            )

        // when
        val response = GetStoreResponse.from(result)

        // then
        assertThat(response.createdAt).isEqualTo(createdTime)
        assertThat(response.updatedAt).isEqualTo(updatedTime)
        assertThat(response.launchedAt).isEqualTo(launchedTime)
    }

    @Test
    @DisplayName("from() - 평점이 소수점 2자리인 Result를 Response로 변환한다")
    fun testFrom_DecimalRating() {
        // given
        val result =
            GetStoreResult(
                storeId = UUID.randomUUID().toString(),
                ownerUserId = UUID.randomUUID().toString(),
                name = "테스트 스토어",
                description = "평점 테스트",
                status = StoreStatus.REGISTERED,
                averageRating = BigDecimal("3.75"),
                reviewCount = 8,
                launchedAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        // when
        val response = GetStoreResponse.from(result)

        // then
        assertThat(response.averageRating).isEqualTo(BigDecimal("3.75"))
    }
}
