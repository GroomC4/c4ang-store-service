package com.groom.store.application.service

import com.groom.store.adapter.out.client.UserResponse
import com.groom.store.adapter.out.client.UserRole
import com.groom.store.adapter.out.persistence.StoreRepository
import com.groom.store.application.dto.DeleteStoreCommand
import com.groom.store.common.TransactionApplier
import com.groom.store.common.annotation.IntegrationTest
import com.groom.store.common.base.StoreBaseServiceIntegrationTest
import com.groom.store.common.enums.StoreStatus
import com.groom.store.common.exception.StoreException
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import java.util.UUID

@IntegrationTest
@SqlGroup(
    Sql(scripts = ["/sql/cleanup-delete-store-service.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/init-delete-store-service.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/cleanup-delete-store-service.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
class DeleteStoreServiceIntegrationTest : StoreBaseServiceIntegrationTest() {
    @Autowired
    private lateinit var deleteStoreService: DeleteStoreService

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

        private val OWNER_USER_ID_4 = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")

        private val OWNER_USER_ID_5 = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")
        private val STORE_ID_5_ALREADY_DELETED = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeee02")
    }

    @Test
    fun `스토어 소유자가 자신의 스토어를 성공적으로 삭제한다`() {
        // given
        every { userServiceClient.get(OWNER_USER_ID_1) } returns
            UserResponse(
                id = OWNER_USER_ID_1,
                name = "Test Owner",
                role = UserRole.OWNER,
            )

        val command =
            DeleteStoreCommand(
                storeId = STORE_ID_1,
                userId = OWNER_USER_ID_1,
            )

        // when
        val result = deleteStoreService.delete(command)

        // then
        assertThat(result).isNotNull
        assertThat(result.storeId).isEqualTo(STORE_ID_1.toString())
        assertThat(result.ownerUserId).isEqualTo(OWNER_USER_ID_1.toString())
        assertThat(result.name).isEqualTo("Store to Delete 1")
        assertThat(result.deletedAt).isNotNull

        // DB에서 실제로 삭제되었는지 검증 (소프트 삭제이므로 DELETED 상태로 변경됨)
        transactionApplier.applyPrimaryTransaction {
            val deletedStore = storeRepository.findById(STORE_ID_1).orElseThrow()
            assertThat(deletedStore.status).isEqualTo(StoreStatus.DELETED)
        }
    }

    @Test
    fun `스토어 소유자가 아닌 사용자가 스토어를 삭제하려고 하면 실패한다`() {
        // given - STORE_ID_2는 OWNER_USER_ID_2 소유이고, OWNER_USER_ID_3가 삭제 시도
        every { userServiceClient.get(OWNER_USER_ID_3) } returns
            UserResponse(
                id = OWNER_USER_ID_3,
                name = "Test Owner",
                role = UserRole.OWNER,
            )

        val command =
            DeleteStoreCommand(
                storeId = STORE_ID_2,
                userId = OWNER_USER_ID_3, // 다른 사용자
            )

        // when & then
        val exception =
            assertThrows<StoreException.StoreAccessDenied> {
                deleteStoreService.delete(command)
            }

        assertThat(exception.storeId).isEqualTo(STORE_ID_2)
        assertThat(exception.userId).isEqualTo(OWNER_USER_ID_3)

        // DB에서 스토어가 삭제되지 않았는지 확인
        transactionApplier.applyPrimaryTransaction {
            val notDeletedStore = storeRepository.findById(STORE_ID_2).orElseThrow()
            assertThat(notDeletedStore.status).isEqualTo(StoreStatus.REGISTERED)
        }
    }

    @Test
    fun `존재하지 않는 스토어를 삭제하려고 하면 실패한다`() {
        // given
        every { userServiceClient.get(OWNER_USER_ID_4) } returns
            UserResponse(
                id = OWNER_USER_ID_4,
                name = "Test Owner",
                role = UserRole.OWNER,
            )

        val nonExistentStoreId = UUID.randomUUID()

        val command =
            DeleteStoreCommand(
                storeId = nonExistentStoreId,
                userId = OWNER_USER_ID_4,
            )

        // when & then
        val exception =
            assertThrows<StoreException.StoreNotFound> {
                deleteStoreService.delete(command)
            }

        assertThat(exception.storeId).isEqualTo(nonExistentStoreId)
    }

    @Test
    fun `이미 삭제된 스토어를 다시 삭제하려고 하면 실패한다`() {
        // given - STORE_ID_5_ALREADY_DELETED는 이미 DELETED 상태
        every { userServiceClient.get(OWNER_USER_ID_5) } returns
            UserResponse(
                id = OWNER_USER_ID_5,
                name = "Test Owner",
                role = UserRole.OWNER,
            )

        val command =
            DeleteStoreCommand(
                storeId = STORE_ID_5_ALREADY_DELETED,
                userId = OWNER_USER_ID_5,
            )

        // when & then
        val exception =
            assertThrows<StoreException.StoreAlreadyDeleted> {
                deleteStoreService.delete(command)
            }

        assertThat(exception.storeId).isEqualTo(STORE_ID_5_ALREADY_DELETED)
    }
}
