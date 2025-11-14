package com.groom.store.application.service

import com.groom.store.application.dto.GetStoreQuery
import com.groom.store.common.TransactionApplier
import com.groom.store.common.annotation.IntegrationTest
import com.groom.store.common.config.MockUserServiceConfig
import com.groom.store.common.enums.StoreStatus
import com.groom.store.common.exception.StoreException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import java.util.UUID

@IntegrationTest
@SpringBootTest
@Import(MockUserServiceConfig::class)
@SqlGroup(
    Sql(scripts = ["/sql/cleanup-get-store-service.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/init-get-store-service.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/cleanup-get-store-service.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
class GetStoreServiceIntegrationTest {
    @Autowired
    private lateinit var getStoreService: GetStoreService

    @Autowired
    private lateinit var transactionApplier: TransactionApplier

    companion object {
        // Fixed UUIDs matching SQL test data
        private val STORE_ID_REGISTERED = UUID.fromString("11111111-1111-1111-1111-111111111113")
        private val OWNER_ID_REGISTERED = UUID.fromString("11111111-1111-1111-1111-111111111111")

        private val STORE_ID_DELETED = UUID.fromString("22222222-2222-2222-2222-222222222224")
        private val OWNER_ID_DELETED = UUID.fromString("22222222-2222-2222-2222-222222222222")

        private val STORE_ID_SUSPENDED = UUID.fromString("33333333-3333-3333-3333-333333333335")
        private val OWNER_ID_SUSPENDED = UUID.fromString("33333333-3333-3333-3333-333333333333")
    }

    @Test
    fun `존재하는 스토어를 성공적으로 조회한다`() {
        // given
        val query = GetStoreQuery(storeId = STORE_ID_REGISTERED)

        // when - applyPrimaryTransaction으로 replica lag 방지
        val result =
            transactionApplier.applyPrimaryTransaction {
                getStoreService.getStore(query)
            }

        // then
        assertThat(result).isNotNull
        assertThat(result.storeId).isEqualTo(STORE_ID_REGISTERED.toString())
        assertThat(result.ownerUserId).isEqualTo(OWNER_ID_REGISTERED.toString())
        assertThat(result.name).isEqualTo("김사장 스토어")
        assertThat(result.description).isEqualTo("멋진 스토어입니다")
        assertThat(result.status).isEqualTo(StoreStatus.REGISTERED)
        assertThat(result.averageRating?.compareTo(java.math.BigDecimal("4.5"))).isEqualTo(0)
        assertThat(result.reviewCount).isEqualTo(100)
        assertThat(result.launchedAt).isNotNull
        assertThat(result.createdAt).isNotNull
        assertThat(result.updatedAt).isNotNull
    }

    @Test
    fun `존재하지 않는 스토어를 조회하면 실패한다`() {
        // given
        val nonExistentStoreId = UUID.randomUUID()
        val query = GetStoreQuery(storeId = nonExistentStoreId)

        // when & then - applyPrimaryTransaction으로 replica lag 방지
        val exception =
            assertThrows<StoreException.StoreNotFound> {
                transactionApplier.applyPrimaryTransaction {
                    getStoreService.getStore(query)
                }
            }

        assertThat(exception.storeId).isEqualTo(nonExistentStoreId)
    }

    @Test
    fun `삭제된 스토어도 조회할 수 있다`() {
        // given
        val query = GetStoreQuery(storeId = STORE_ID_DELETED)

        // when - applyPrimaryTransaction으로 replica lag 방지
        val result =
            transactionApplier.applyPrimaryTransaction {
                getStoreService.getStore(query)
            }

        // then
        assertThat(result).isNotNull
        assertThat(result.storeId).isEqualTo(STORE_ID_DELETED.toString())
        assertThat(result.ownerUserId).isEqualTo(OWNER_ID_DELETED.toString())
        assertThat(result.status).isEqualTo(StoreStatus.DELETED)
        assertThat(result.name).isEqualTo("삭제된 스토어")
        assertThat(result.description).isEqualTo("삭제됨")
    }

    @Test
    fun `일시정지된 스토어도 조회할 수 있다`() {
        // given
        val query = GetStoreQuery(storeId = STORE_ID_SUSPENDED)

        // when - applyPrimaryTransaction으로 replica lag 방지
        val result =
            transactionApplier.applyPrimaryTransaction {
                getStoreService.getStore(query)
            }

        // then
        assertThat(result).isNotNull
        assertThat(result.storeId).isEqualTo(STORE_ID_SUSPENDED.toString())
        assertThat(result.ownerUserId).isEqualTo(OWNER_ID_SUSPENDED.toString())
        assertThat(result.status).isEqualTo(StoreStatus.SUSPENDED)
        assertThat(result.name).isEqualTo("일시정지된 스토어")
        assertThat(result.description).isEqualTo("일시정지됨")
    }
}
