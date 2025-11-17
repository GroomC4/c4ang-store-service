package com.groom.store.adapter.out.persistence

import com.groom.store.domain.model.Store
import com.groom.store.domain.model.StoreRating
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * StoreRatingRepository JPA 레이어 테스트
 *
 * 실제 데이터베이스(Testcontainers)를 사용하여 JPA Repository의 CRUD 동작을 검증합니다.
 */
@DisplayName("StoreRatingRepository 테스트")
class StoreRatingRepositoryTest : BaseRepositoryTest() {
    @Autowired
    private lateinit var storeRatingRepository: StoreRatingRepository

    @Autowired
    private lateinit var storeRepository: StoreRepository

    @Test
    @DisplayName("스토어 평점을 저장하고 ID로 조회할 수 있다")
    fun testSaveAndFindById() {
        // given
        val store = createAndSaveStore("평점 테스트 스토어")
        val rating =
            StoreRating(
                averageRating = BigDecimal("4.50"),
                reviewCount = 10,
                launchedAt = LocalDateTime.now(),
            ).apply {
                this.store = store
            }

        // when
        val savedRating = storeRatingRepository.save(rating)
        val foundRating = storeRatingRepository.findById(savedRating.id)

        // then
        assertThat(foundRating).isPresent
        assertThat(foundRating.get().id).isEqualTo(savedRating.id)
        assertThat(foundRating.get().averageRating).isEqualByComparingTo(BigDecimal("4.50"))
        assertThat(foundRating.get().reviewCount).isEqualTo(10)
        assertThat(foundRating.get().store?.id).isEqualTo(store.id)
    }

    @Test
    @DisplayName("스토어 ID로 평점을 조회할 수 있다")
    fun testFindByStoreId() {
        // given
        val store = createAndSaveStore("스토어 ID 검색 테스트")
        val rating =
            StoreRating(
                averageRating = BigDecimal("3.75"),
                reviewCount = 20,
                launchedAt = LocalDateTime.now(),
            ).apply {
                this.store = store
            }
        storeRatingRepository.save(rating)

        // when
        val foundRating = storeRatingRepository.findByStore_Id(store.id)

        // then
        assertThat(foundRating).isPresent
        assertThat(foundRating.get().averageRating).isEqualByComparingTo(BigDecimal("3.75"))
        assertThat(foundRating.get().reviewCount).isEqualTo(20)
        assertThat(foundRating.get().store?.id).isEqualTo(store.id)
    }

    @Test
    @DisplayName("존재하지 않는 스토어 ID로 조회 시 빈 Optional을 반환한다")
    fun testFindByNonExistentStoreId() {
        // given
        val nonExistentStoreId = UUID.randomUUID()

        // when
        val foundRating = storeRatingRepository.findByStore_Id(nonExistentStoreId)

        // then
        assertThat(foundRating).isEmpty
    }

    @Test
    @DisplayName("평점 정보를 수정하고 저장할 수 있다")
    fun testUpdateRating() {
        // given
        val store = createAndSaveStore("평점 수정 테스트")
        val rating =
            StoreRating(
                averageRating = BigDecimal("4.00"),
                reviewCount = 5,
                launchedAt = LocalDateTime.now(),
            ).apply {
                this.store = store
            }
        val savedRating = storeRatingRepository.save(rating)

        // when: 새로운 평점 객체 생성 (불변 객체 패턴)
        val updatedRating =
            StoreRating(
                averageRating = BigDecimal("4.25"),
                reviewCount = 6,
                launchedAt = savedRating.launchedAt,
            ).apply {
                this.id = savedRating.id
                this.store = savedRating.store
            }
        storeRatingRepository.save(updatedRating)

        // then
        val foundRating = storeRatingRepository.findById(savedRating.id)
        assertThat(foundRating).isPresent
        assertThat(foundRating.get().averageRating).isEqualByComparingTo(BigDecimal("4.25"))
        assertThat(foundRating.get().reviewCount).isEqualTo(6)
    }

    @Test
    @DisplayName("평점을 삭제할 수 있다")
    fun testDeleteRating() {
        // given
        val store = createAndSaveStore("평점 삭제 테스트")
        val rating =
            StoreRating(
                averageRating = BigDecimal("5.00"),
                reviewCount = 1,
                launchedAt = LocalDateTime.now(),
            ).apply {
                this.store = store
            }
        val savedRating = storeRatingRepository.save(rating)
        val ratingId = savedRating.id

        // when
        storeRatingRepository.delete(savedRating)

        // then
        val foundRating = storeRatingRepository.findById(ratingId)
        assertThat(foundRating).isEmpty
    }

    @Test
    @DisplayName("평점 0점, 리뷰 0개인 평점을 저장할 수 있다")
    fun testSaveZeroRating() {
        // given
        val store = createAndSaveStore("평점 없는 신규 스토어")
        val rating =
            StoreRating(
                averageRating = BigDecimal.ZERO,
                reviewCount = 0,
                launchedAt = LocalDateTime.now(),
            ).apply {
                this.store = store
            }

        // when
        val savedRating = storeRatingRepository.save(rating)

        // then
        val foundRating = storeRatingRepository.findById(savedRating.id)
        assertThat(foundRating).isPresent
        assertThat(foundRating.get().averageRating).isEqualByComparingTo(BigDecimal.ZERO)
        assertThat(foundRating.get().reviewCount).isEqualTo(0)
    }

    private fun createAndSaveStore(name: String): Store =
        storeRepository.save(
            Store(
                ownerUserId = UUID.randomUUID(),
                name = name,
                description = "테스트 설명",
            ),
        )
}
