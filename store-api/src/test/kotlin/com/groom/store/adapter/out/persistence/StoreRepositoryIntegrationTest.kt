package com.groom.store.adapter.out.persistence

import com.groom.store.common.IntegrationTestBase
import com.groom.store.common.TransactionApplier
import com.groom.store.common.enums.StoreStatus
import com.groom.store.domain.model.Store
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import java.time.LocalDateTime
import java.util.UUID

/**
 * StoreRepository 통합 테스트
 *
 * IntegrationTestBase를 상속받아 @SpringBootTest와 Testcontainers 설정을 재사용합니다.
 */
@SqlGroup(
    Sql(scripts = ["/sql/cleanup-store-repository.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/init-store-repository.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/cleanup-store-repository.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
class StoreRepositoryIntegrationTest : IntegrationTestBase() {
    @Autowired
    private lateinit var storeRepository: StoreRepository

    @Autowired
    private lateinit var transactionApplier: TransactionApplier

    companion object {
        // Test data from SQL script
        private val OWNER_USER_ID_1 = UUID.fromString("11111111-1111-1111-1111-111111111111")
        private val STORE_ID_1 = UUID.fromString("11111111-1111-1111-1111-111111111113")

        private val OWNER_USER_ID_2 = UUID.fromString("22222222-2222-2222-2222-222222222222")
        private val STORE_ID_2 = UUID.fromString("22222222-2222-2222-2222-222222222224")

        private val OWNER_USER_ID_3 = UUID.fromString("33333333-3333-3333-3333-333333333333")
        private val STORE_ID_3 = UUID.fromString("33333333-3333-3333-3333-333333333335")

        private val OWNER_USER_ID_WITHOUT_STORE = UUID.fromString("99999999-9999-9999-9999-999999999999")
    }

    @Test
    fun `findByOwnerUserId로 스토어를 조회할 수 있다`() {
        // when
        val result =
            transactionApplier.applyPrimaryTransaction {
                storeRepository.findByOwnerUserId(OWNER_USER_ID_1)
            }

        // then
        assertThat(result).isPresent
        assertThat(result.get().id).isEqualTo(STORE_ID_1)
        assertThat(result.get().ownerUserId).isEqualTo(OWNER_USER_ID_1)
        assertThat(result.get().name).isEqualTo("김사장 스토어")
        assertThat(result.get().status).isEqualTo(StoreStatus.REGISTERED)
    }

    @Test
    fun `findByOwnerUserId로 스토어가 없는 경우 empty를 반환한다`() {
        // when
        val result =
            transactionApplier.applyPrimaryTransaction {
                storeRepository.findByOwnerUserId(OWNER_USER_ID_WITHOUT_STORE)
            }

        // then
        assertThat(result).isEmpty
    }

    @Test
    fun `findByStatus로 특정 상태의 스토어들을 조회할 수 있다`() {
        // when
        val result =
            transactionApplier.applyPrimaryTransaction {
                storeRepository.findByStatus(StoreStatus.REGISTERED)
            }

        // then
        assertThat(result).isNotEmpty
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(STORE_ID_1)
        assertThat(result[0].status).isEqualTo(StoreStatus.REGISTERED)
    }

    @Test
    fun `findByStatus로 DELETED 상태의 스토어를 조회할 수 있다`() {
        // when
        val result =
            transactionApplier.applyPrimaryTransaction {
                storeRepository.findByStatus(StoreStatus.DELETED)
            }

        // then
        assertThat(result).isNotEmpty
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(STORE_ID_2)
        assertThat(result[0].status).isEqualTo(StoreStatus.DELETED)
    }

    @Test
    fun `findByStatus로 SUSPENDED 상태의 스토어를 조회할 수 있다`() {
        // when
        val result =
            transactionApplier.applyPrimaryTransaction {
                storeRepository.findByStatus(StoreStatus.SUSPENDED)
            }

        // then
        assertThat(result).isNotEmpty
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(STORE_ID_3)
        assertThat(result[0].status).isEqualTo(StoreStatus.SUSPENDED)
    }

    @Test
    fun `findByNameContaining으로 이름에 특정 문자열이 포함된 스토어들을 조회할 수 있다`() {
        // when
        val result =
            transactionApplier.applyPrimaryTransaction {
                storeRepository.findByNameContaining("김사장")
            }

        // then
        assertThat(result).isNotEmpty
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(STORE_ID_1)
        assertThat(result[0].name).contains("김사장")
    }

    @Test
    fun `findByNameContaining으로 부분 검색이 가능하다`() {
        // when
        val result =
            transactionApplier.applyPrimaryTransaction {
                storeRepository.findByNameContaining("스토어")
            }

        // then - 모든 스토어 이름에 "스토어"가 포함됨
        assertThat(result).isNotEmpty
        assertThat(result).hasSizeGreaterThanOrEqualTo(3)
        assertThat(result.all { it.name.contains("스토어") }).isTrue()
    }

    @Test
    fun `findByNameContaining으로 일치하는 스토어가 없으면 빈 리스트를 반환한다`() {
        // when
        val result =
            transactionApplier.applyPrimaryTransaction {
                storeRepository.findByNameContaining("존재하지않는이름")
            }

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `existsByOwnerUserId로 특정 사용자의 스토어 존재 여부를 확인할 수 있다`() {
        // when
        val exists =
            transactionApplier.applyPrimaryTransaction {
                storeRepository.existsByOwnerUserId(OWNER_USER_ID_1)
            }

        // then
        assertThat(exists).isTrue()
    }

    @Test
    fun `existsByOwnerUserId로 스토어가 없는 사용자는 false를 반환한다`() {
        // when
        val exists =
            transactionApplier.applyPrimaryTransaction {
                storeRepository.existsByOwnerUserId(OWNER_USER_ID_WITHOUT_STORE)
            }

        // then
        assertThat(exists).isFalse()
    }

    @Test
    fun `findByIdIn으로 여러 스토어를 한 번에 조회할 수 있다`() {
        // given
        val storeIds = listOf(STORE_ID_1, STORE_ID_2, STORE_ID_3)

        // when
        val result =
            transactionApplier.applyPrimaryTransaction {
                storeRepository.findByIdIn(storeIds)
            }

        // then
        assertThat(result).hasSize(3)
        assertThat(result.map { it.id }).containsExactlyInAnyOrder(STORE_ID_1, STORE_ID_2, STORE_ID_3)
    }

    @Test
    fun `findByIdIn으로 일부만 존재하는 경우 존재하는 스토어만 반환한다`() {
        // given
        val nonExistentId = UUID.randomUUID()
        val storeIds = listOf(STORE_ID_1, nonExistentId, STORE_ID_2)

        // when
        val result =
            transactionApplier.applyPrimaryTransaction {
                storeRepository.findByIdIn(storeIds)
            }

        // then
        assertThat(result).hasSize(2)
        assertThat(result.map { it.id }).containsExactlyInAnyOrder(STORE_ID_1, STORE_ID_2)
    }

    @Test
    fun `findByIdIn으로 빈 컬렉션을 전달하면 빈 리스트를 반환한다`() {
        // when
        val result =
            transactionApplier.applyPrimaryTransaction {
                storeRepository.findByIdIn(emptyList())
            }

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `save로 새로운 스토어를 저장할 수 있다`() {
        // given
        val newOwnerId = UUID.randomUUID()
        val newStoreId = UUID.randomUUID()
        val newStore =
            Store(
                ownerUserId = newOwnerId,
                name = "New Store",
                description = "New Description",
                status = StoreStatus.REGISTERED,
            ).apply {
                this.id = newStoreId
            }

        // when
        val saved =
            transactionApplier.applyPrimaryTransaction {
                storeRepository.save(newStore)
            }

        // then
        assertThat(saved).isNotNull
        assertThat(saved.id).isEqualTo(newStoreId)
        assertThat(saved.ownerUserId).isEqualTo(newOwnerId)
        assertThat(saved.name).isEqualTo("New Store")

        // 실제로 저장되었는지 확인
        transactionApplier.applyPrimaryTransaction {
            val found = storeRepository.findById(newStoreId)
            assertThat(found).isPresent
            assertThat(found.get().name).isEqualTo("New Store")
        }
    }
}
