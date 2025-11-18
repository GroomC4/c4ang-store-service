package com.groom.store.adapter.out.persistence

import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * StoreRepository JPA 레이어 테스트
 *
 * 실제 데이터베이스(Testcontainers)를 사용하여 JPA Repository의 CRUD 동작을 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("StoreRepository 테스트")
class StoreRepositoryTest {
    @Autowired
    private lateinit var storeRepository: StoreRepository

    @Test
    @DisplayName("스토어를 저장하고 ID로 조회할 수 있다")
    fun testSaveAndFindById() {
        // given
        val ownerUserId = UUID.randomUUID()
        val store =
            Store(
                ownerUserId = ownerUserId,
                name = "테스트 스토어",
                description = "테스트 설명",
                status = StoreStatus.REGISTERED,
            )

        // when
        val savedStore = storeRepository.save(store)
        val foundStore = storeRepository.findById(savedStore.id)

        // then
        assertThat(foundStore).isPresent
        assertThat(foundStore.get().id).isEqualTo(savedStore.id)
        assertThat(foundStore.get().ownerUserId).isEqualTo(ownerUserId)
        assertThat(foundStore.get().name).isEqualTo("테스트 스토어")
        assertThat(foundStore.get().description).isEqualTo("테스트 설명")
        assertThat(foundStore.get().status).isEqualTo(StoreStatus.REGISTERED)
    }

    @Test
    @DisplayName("ownerUserId로 스토어를 조회할 수 있다")
    fun testFindByOwnerUserId() {
        // given
        val ownerUserId = UUID.randomUUID()
        val store =
            Store(
                ownerUserId = ownerUserId,
                name = "소유자의 스토어",
                description = "소유자 검색 테스트",
            )
        storeRepository.save(store)

        // when
        val foundStore = storeRepository.findByOwnerUserId(ownerUserId)

        // then
        assertThat(foundStore).isPresent
        assertThat(foundStore.get().ownerUserId).isEqualTo(ownerUserId)
        assertThat(foundStore.get().name).isEqualTo("소유자의 스토어")
    }

    @Test
    @DisplayName("존재하지 않는 ownerUserId로 조회 시 빈 Optional을 반환한다")
    fun testFindByNonExistentOwnerUserId() {
        // given
        val nonExistentUserId = UUID.randomUUID()

        // when
        val foundStore = storeRepository.findByOwnerUserId(nonExistentUserId)

        // then
        assertThat(foundStore).isEmpty
    }

    @Test
    @DisplayName("상태별로 스토어를 조회할 수 있다")
    fun testFindByStatus() {
        // given
        val owner1 = UUID.randomUUID()
        val owner2 = UUID.randomUUID()
        val owner3 = UUID.randomUUID()

        storeRepository.save(
            Store(
                ownerUserId = owner1,
                name = "등록된 스토어 1",
                status = StoreStatus.REGISTERED,
            ),
        )
        storeRepository.save(
            Store(
                ownerUserId = owner2,
                name = "등록된 스토어 2",
                status = StoreStatus.REGISTERED,
            ),
        )
        storeRepository.save(
            Store(
                ownerUserId = owner3,
                name = "일시정지 스토어",
                status = StoreStatus.SUSPENDED,
            ),
        )

        // when
        val registeredStores = storeRepository.findByStatus(StoreStatus.REGISTERED)
        val suspendedStores = storeRepository.findByStatus(StoreStatus.SUSPENDED)

        // then
        assertThat(registeredStores).hasSize(2)
        assertThat(registeredStores.map { it.name }).containsExactlyInAnyOrder("등록된 스토어 1", "등록된 스토어 2")
        assertThat(suspendedStores).hasSize(1)
        assertThat(suspendedStores[0].name).isEqualTo("일시정지 스토어")
    }

    @Test
    @DisplayName("이름으로 스토어를 부분 검색할 수 있다")
    fun testFindByNameContaining() {
        // given
        val owner1 = UUID.randomUUID()
        val owner2 = UUID.randomUUID()
        val owner3 = UUID.randomUUID()

        storeRepository.save(
            Store(
                ownerUserId = owner1,
                name = "테크 스토어",
            ),
        )
        storeRepository.save(
            Store(
                ownerUserId = owner2,
                name = "테크 마켓",
            ),
        )
        storeRepository.save(
            Store(
                ownerUserId = owner3,
                name = "푸드 마켓",
            ),
        )

        // when
        val techStores = storeRepository.findByNameContaining("테크")
        val marketStores = storeRepository.findByNameContaining("마켓")

        // then
        assertThat(techStores).hasSize(2)
        assertThat(techStores.map { it.name }).containsExactlyInAnyOrder("테크 스토어", "테크 마켓")
        assertThat(marketStores).hasSize(2)
        assertThat(marketStores.map { it.name }).containsExactlyInAnyOrder("테크 마켓", "푸드 마켓")
    }

    @Test
    @DisplayName("ownerUserId의 스토어 존재 여부를 확인할 수 있다")
    fun testExistsByOwnerUserId() {
        // given
        val ownerWithStore = UUID.randomUUID()
        val ownerWithoutStore = UUID.randomUUID()

        storeRepository.save(
            Store(
                ownerUserId = ownerWithStore,
                name = "기존 스토어",
            ),
        )

        // when & then
        assertThat(storeRepository.existsByOwnerUserId(ownerWithStore)).isTrue()
        assertThat(storeRepository.existsByOwnerUserId(ownerWithoutStore)).isFalse()
    }

    @Test
    @DisplayName("여러 ID로 스토어 목록을 조회할 수 있다")
    fun testFindByIdIn() {
        // given
        val store1 =
            storeRepository.save(
                Store(
                    ownerUserId = UUID.randomUUID(),
                    name = "스토어 1",
                ),
            )
        val store2 =
            storeRepository.save(
                Store(
                    ownerUserId = UUID.randomUUID(),
                    name = "스토어 2",
                ),
            )
        val store3 =
            storeRepository.save(
                Store(
                    ownerUserId = UUID.randomUUID(),
                    name = "스토어 3",
                ),
            )

        // when
        val stores = storeRepository.findByIdIn(listOf(store1.id, store2.id))

        // then
        assertThat(stores).hasSize(2)
        assertThat(stores.map { it.id }).containsExactlyInAnyOrder(store1.id, store2.id)
        assertThat(stores.map { it.name }).containsExactlyInAnyOrder("스토어 1", "스토어 2")
    }

    @Test
    @DisplayName("스토어를 수정하고 저장할 수 있다")
    fun testUpdateStore() {
        // given
        val store =
            storeRepository.save(
                Store(
                    ownerUserId = UUID.randomUUID(),
                    name = "원래 이름",
                    description = "원래 설명",
                    status = StoreStatus.REGISTERED,
                ),
            )

        // when
        val updateResult =
            store.updateInfo(
                name = "변경된 이름",
                description = "변경된 설명",
            )
        val updatedStore = storeRepository.save(updateResult.updatedStore)

        // then
        val foundStore = storeRepository.findById(updatedStore.id)
        assertThat(foundStore).isPresent
        assertThat(foundStore.get().name).isEqualTo("변경된 이름")
        assertThat(foundStore.get().description).isEqualTo("변경된 설명")
    }

    @Test
    @DisplayName("스토어를 삭제할 수 있다")
    fun testDeleteStore() {
        // given
        val store =
            storeRepository.save(
                Store(
                    ownerUserId = UUID.randomUUID(),
                    name = "삭제할 스토어",
                ),
            )
        val storeId = store.id

        // when
        storeRepository.delete(store)

        // then
        val foundStore = storeRepository.findById(storeId)
        assertThat(foundStore).isEmpty
    }

    @Test
    @DisplayName("모든 스토어를 조회할 수 있다")
    fun testFindAll() {
        // given
        storeRepository.deleteAll()
        storeRepository.save(
            Store(
                ownerUserId = UUID.randomUUID(),
                name = "스토어 A",
            ),
        )
        storeRepository.save(
            Store(
                ownerUserId = UUID.randomUUID(),
                name = "스토어 B",
            ),
        )

        // when
        val allStores = storeRepository.findAll()

        // then
        assertThat(allStores).hasSize(2)
        assertThat(allStores.map { it.name }).containsExactlyInAnyOrder("스토어 A", "스토어 B")
    }
}
