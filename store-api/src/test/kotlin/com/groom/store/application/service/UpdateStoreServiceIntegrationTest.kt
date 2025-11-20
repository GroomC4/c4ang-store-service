package com.groom.store.application.service

import com.groom.store.adapter.out.persistence.StoreRepository
import com.groom.store.application.dto.UpdateStoreCommand
import com.groom.store.common.TransactionApplier
import com.groom.store.common.base.StoreBaseServiceIntegrationTest
import com.groom.store.common.exception.StoreException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import java.util.UUID

@SqlGroup(
    Sql(scripts = ["/sql/cleanup-update-store-service.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/init-update-store-service.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/cleanup-update-store-service.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
class UpdateStoreServiceIntegrationTest : StoreBaseServiceIntegrationTest() {
    @Autowired
    private lateinit var updateService: UpdateService

    @Autowired
    private lateinit var storeRepository: StoreRepository

    @Autowired
    private lateinit var transactionApplier: TransactionApplier

    companion object {
        // Test user and store IDs from SQL scripts
        private val OWNER_USER_ID_1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        private val STORE_ID_1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa02")

        private val OWNER_USER_ID_2 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
        private val STORE_ID_2 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb02")

        private val OWNER_USER_ID_3 = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")
        private val STORE_ID_3 = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccc02")
        private val OWNER_USER_ID_4 = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")

        private val OWNER_USER_ID_5 = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")

        private val OWNER_USER_ID_6 = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")
        private val STORE_ID_6 = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffff02")
    }

    @Test
    fun `스토어 소유자가 자신의 스토어를 성공적으로 수정한다`() {
        // given
        val command =
            UpdateStoreCommand(
                storeId = STORE_ID_1,
                userId = OWNER_USER_ID_1,
                name = "수정된 스토어",
                description = "수정된 설명",
            )

        // when
        val result = updateService.update(command)

        // then - 반환값 검증
        assertThat(result).isNotNull
        assertThat(result.storeId).isEqualTo(STORE_ID_1.toString())
        assertThat(result.ownerUserId).isEqualTo(OWNER_USER_ID_1.toString())
        assertThat(result.name).isEqualTo("수정된 스토어")
        assertThat(result.description).isEqualTo("수정된 설명")
        assertThat(result.status).isEqualTo("REGISTERED")
        assertThat(result.updatedAt).isNotNull

        // then - DB에 실제 수정되었는지 검증 (별도 트랜잭션에서 조회)
        transactionApplier.applyPrimaryTransaction {
            val savedStore = storeRepository.findById(STORE_ID_1).orElseThrow()
            assertThat(savedStore.name).isEqualTo("수정된 스토어")
            assertThat(savedStore.description).isEqualTo("수정된 설명")
            assertThat(savedStore.ownerUserId).isEqualTo(OWNER_USER_ID_1)
            assertThat(savedStore.updatedAt).isNotNull
        }
    }

    @Test
    fun `description을 null로 수정할 수 있다`() {
        // given
        val command =
            UpdateStoreCommand(
                storeId = STORE_ID_2,
                userId = OWNER_USER_ID_2,
                name = "수정된 스토어",
                description = null,
            )

        // when
        val result = updateService.update(command)

        // then
        assertThat(result.description).isNull()

        transactionApplier.applyPrimaryTransaction {
            val savedStore = storeRepository.findById(STORE_ID_2).orElseThrow()
            assertThat(savedStore.description).isNull()
            assertThat(savedStore.name).isEqualTo("수정된 스토어")
        }
    }

    @Test
    fun `스토어 소유자가 아닌 사용자가 스토어를 수정하려고 하면 실패한다`() {
        // given - STORE_ID_3는 OWNER_USER_ID_3 소유이고, OWNER_USER_ID_4가 수정 시도
        val command =
            UpdateStoreCommand(
                storeId = STORE_ID_3,
                userId = OWNER_USER_ID_4, // 다른 사용자
                name = "수정 시도",
                description = "수정 실패",
            )

        // when & then
        val exception =
            assertThrows<StoreException.StoreAccessDenied> {
                updateService.update(command)
            }

        assertThat(exception.storeId).isEqualTo(STORE_ID_3)
        assertThat(exception.userId).isEqualTo(OWNER_USER_ID_4)
        assertThat(exception.message).isEqualTo("스토어에 대한 접근 권한이 없습니다")

        // then - DB에 스토어가 수정되지 않았는지 확인
        transactionApplier.applyPrimaryTransaction {
            val savedStore = storeRepository.findById(STORE_ID_3).orElseThrow()
            assertThat(savedStore.name).isEqualTo("Original Store 3") // 변경되지 않음
        }
    }

    @Test
    fun `존재하지 않는 스토어를 수정하려고 하면 실패한다`() {
        // given
        val nonExistentStoreId = UUID.randomUUID()

        val command =
            UpdateStoreCommand(
                storeId = nonExistentStoreId,
                userId = OWNER_USER_ID_5,
                name = "수정 시도",
                description = "수정 실패",
            )

        // when & then
        val exception =
            assertThrows<StoreException.StoreNotFound> {
                updateService.update(command)
            }

        assertThat(exception.storeId).isEqualTo(nonExistentStoreId)
        assertThat(exception.message).contains("스토어를 찾을 수 없습니다")
    }

    @Test
    fun `스토어 이름만 수정하고 description은 유지할 수 있다`() {
        // given
        val originalDescription = "Keep this description"

        val command =
            UpdateStoreCommand(
                storeId = STORE_ID_6,
                userId = OWNER_USER_ID_6,
                name = "새로운 이름",
                description = originalDescription, // 동일한 설명 유지
            )

        // when
        val result = updateService.update(command)

        // then
        assertThat(result.name).isEqualTo("새로운 이름")
        assertThat(result.description).isEqualTo(originalDescription)

        transactionApplier.applyPrimaryTransaction {
            val savedStore = storeRepository.findById(STORE_ID_6).orElseThrow()
            assertThat(savedStore.name).isEqualTo("새로운 이름")
            assertThat(savedStore.description).isEqualTo(originalDescription)
        }
    }
}
