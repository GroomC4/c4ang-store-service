package com.groom.store.application.service

import com.groom.ecommerce.common.TransactionApplier
import com.groom.ecommerce.common.exception.StoreException
import com.groom.ecommerce.common.exception.UserException
import com.groom.ecommerce.store.application.dto.RegisterStoreCommand
import com.groom.ecommerce.store.common.enums.StoreAuditEventType
import com.groom.ecommerce.store.common.enums.StoreStatus
import com.groom.ecommerce.store.infrastructure.repository.StoreAuditRepositoryImpl
import com.groom.store.application.service.RegisterService
import com.groom.store.common.annotation.IntegrationTest
import com.groom.store.outbound.repository.StoreRepositoryImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import java.util.UUID

@IntegrationTest
@SpringBootTest
@SqlGroup(
    Sql(scripts = ["/sql/store/cleanup-register-store-service.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/store/init-register-store-service.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["/sql/store/cleanup-register-store-service.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
class RegisterStoreServiceIntegrationTest {
    @Autowired
    private lateinit var registerService: RegisterService

    @Autowired
    private lateinit var storeRepository: StoreRepositoryImpl

    @Autowired
    private lateinit var storeAuditRepository: StoreAuditRepositoryImpl

    @Autowired
    private lateinit var transactionApplier: TransactionApplier

    companion object {
        // Test user IDs from SQL scripts
        private val OWNER_USER_ID_1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        private val CUSTOMER_USER_ID_1 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
        private val OWNER_USER_ID_2 = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")
        private val EXISTING_STORE_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccc02")
        private val OWNER_USER_ID_3 = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")
        private val OWNER_USER_ID_4 = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")
        private val OWNER_USER_ID_5 = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")
    }

    @Test
    fun `OWNER 역할을 가진 사용자가 스토어를 성공적으로 등록한다`() {
        // given
        val command =
            RegisterStoreCommand(
                ownerUserId = OWNER_USER_ID_1,
                name = "테크 스토어",
                description = "최신 전자제품 판매",
            )

        // when
        val result = registerService.register(command)

        // then - 반환값 검증
        assertThat(result).isNotNull
        assertThat(result.storeId).isNotNull
        assertThat(result.ownerUserId).isEqualTo(OWNER_USER_ID_1.toString())
        assertThat(result.name).isEqualTo("테크 스토어")
        assertThat(result.description).isEqualTo("최신 전자제품 판매")
        assertThat(result.status).isEqualTo("REGISTERED")
        assertThat(result.createdAt).isNotNull

        // then - DB에 실제 저장되었는지 검증
        val savedStore = storeRepository.findByOwnerUserId(OWNER_USER_ID_1).orElseThrow()

        // 반환된 storeId와 DB의 storeId가 일치하는지 확인
        assertThat(savedStore.id.toString()).isEqualTo(result.storeId)

        assertThat(savedStore.id).isNotNull
        assertThat(savedStore.ownerUserId).isEqualTo(OWNER_USER_ID_1)
        assertThat(savedStore.name).isEqualTo("테크 스토어")
        assertThat(savedStore.description).isEqualTo("최신 전자제품 판매")
        assertThat(savedStore.status).isEqualTo(StoreStatus.REGISTERED)
        assertThat(savedStore.createdAt).isNotNull

        // then - StoreRating도 함께 생성되었는지 검증
        assertThat(savedStore.rating).isNotNull
        assertThat(savedStore.rating!!.store).isEqualTo(savedStore)
        assertThat(savedStore.rating!!.averageRating).isNotNull
        assertThat(savedStore.rating!!.reviewCount).isEqualTo(0)
    }

    @Test
    fun `CUSTOMER 역할 사용자가 스토어 등록을 시도하면 실패한다`() {
        // given
        val command =
            RegisterStoreCommand(
                ownerUserId = CUSTOMER_USER_ID_1,
                name = "실패할 스토어",
                description = "등록 불가",
            )

        // when & then
        val exception =
            assertThrows<UserException.InsufficientPermission> {
                registerService.register(command)
            }

        assertThat(exception.userId).isEqualTo(CUSTOMER_USER_ID_1)
        assertThat(exception.requiredRole).isEqualTo("OWNER")
        assertThat(exception.currentRole).isEqualTo("CUSTOMER")
        assertThat(exception.message).isEqualTo("이 작업을 수행할 권한이 없습니다.")

        // then - DB에 스토어가 생성되지 않았는지 확인
        val storeOptional = storeRepository.findByOwnerUserId(CUSTOMER_USER_ID_1)
        assertThat(storeOptional).isEmpty
    }

    @Test
    fun `이미 스토어를 보유한 OWNER가 중복 등록을 시도하면 실패한다`() {
        // given - OWNER_USER_ID_2는 이미 EXISTING_STORE_ID를 소유
        val command =
            RegisterStoreCommand(
                ownerUserId = OWNER_USER_ID_2,
                name = "두 번째 스토어",
                description = "중복 등록 시도",
            )

        // when & then - StoreException.DuplicateStore 예외 발생
        val exception =
            assertThrows<StoreException.DuplicateStore> {
                registerService.register(command)
            }

        assertThat(exception.ownerUserId).isEqualTo(OWNER_USER_ID_2)
        assertThat(exception.message).isEqualTo("이미 스토어가 존재합니다. 판매자당 하나의 스토어만 운영할 수 있습니다")

        // then - DB에 스토어가 하나만 존재하는지 확인
        val storeOptional = storeRepository.findByOwnerUserId(OWNER_USER_ID_2)
        assertThat(storeOptional).isPresent
        assertThat(storeOptional.get().name).isEqualTo("Existing Store")
        assertThat(storeOptional.get().id).isEqualTo(EXISTING_STORE_ID)
    }

    @Test
    fun `존재하지 않는 사용자가 스토어 등록을 시도하면 실패한다`() {
        // given
        val nonExistentUserId = UUID.randomUUID()
        val command =
            RegisterStoreCommand(
                ownerUserId = nonExistentUserId,
                name = "존재하지 않는 사용자의 스토어",
                description = "실패할 것임",
            )

        // when & then
        val exception =
            assertThrows<UserException.UserNotFound> {
                registerService.register(command)
            }

        assertThat(exception.userId).isEqualTo(nonExistentUserId)
        assertThat(exception.message).contains("사용자를 찾을 수 없습니다")

        // then - DB에 스토어가 생성되지 않았는지 확인
        val storeOptional = storeRepository.findByOwnerUserId(nonExistentUserId)
        assertThat(storeOptional).isEmpty
    }

    @Test
    fun `등록된 스토어가 DB에 올바르게 저장되고 모든 필드가 정확하게 유지된다`() {
        // given
        val command =
            RegisterStoreCommand(
                ownerUserId = OWNER_USER_ID_3,
                name = "프리미엄 스토어",
                description = "고급 상품만 판매하는 스토어입니다",
            )

        // when
        val result = registerService.register(command)

        // then - DB에서 직접 조회하여 검증
        val savedStore = storeRepository.findByOwnerUserId(OWNER_USER_ID_3).orElseThrow()

        // 반환된 storeId와 DB의 storeId가 일치하는지 확인
        assertThat(savedStore.id.toString()).isEqualTo(result.storeId)

        // Store 엔티티 필드 검증
        assertThat(savedStore.id).isNotNull
        assertThat(savedStore.ownerUserId).isEqualTo(OWNER_USER_ID_3)
        assertThat(savedStore.name).isEqualTo("프리미엄 스토어")
        assertThat(savedStore.description).isEqualTo("고급 상품만 판매하는 스토어입니다")
        assertThat(savedStore.status).isEqualTo(StoreStatus.REGISTERED)
        assertThat(savedStore.hiddenAt).isNull()
        assertThat(savedStore.deletedAt).isNull()
        assertThat(savedStore.createdAt).isNotNull
        assertThat(savedStore.updatedAt).isNotNull

        // StoreRating 양방향 관계 검증
        assertThat(savedStore.rating).isNotNull
        assertThat(savedStore.rating!!.id).isNotNull
        assertThat(savedStore.rating!!.store).isEqualTo(savedStore)
        assertThat(savedStore.rating!!.launchedAt).isNotNull

        // ownerUserId로 조회 가능한지 검증
        val storeByOwner = storeRepository.findByOwnerUserId(OWNER_USER_ID_3).orElseThrow()
        assertThat(storeByOwner.id).isEqualTo(savedStore.id)

        // existsByOwnerUserId로도 확인 가능한지 검증
        assertThat(storeRepository.existsByOwnerUserId(OWNER_USER_ID_3)).isTrue
    }

    @Test
    fun `description이 null인 경우에도 스토어가 정상적으로 등록된다`() {
        // given
        val command =
            RegisterStoreCommand(
                ownerUserId = OWNER_USER_ID_4,
                name = "간단한 스토어",
                description = null,
            )

        // when
        val result = registerService.register(command)

        // then
        assertThat(result.description).isNull()

        val savedStore = storeRepository.findByOwnerUserId(OWNER_USER_ID_4).orElseThrow()
        assertThat(savedStore.id.toString()).isEqualTo(result.storeId)
        assertThat(savedStore.description).isNull()
        assertThat(savedStore.name).isEqualTo("간단한 스토어")
        assertThat(savedStore.rating).isNotNull
    }

    @Test
    fun `스토어 등록 시 감사 로그가 생성된다`() {
        // given
        val command =
            RegisterStoreCommand(
                ownerUserId = OWNER_USER_ID_5,
                name = "감사로그 테스트 스토어",
                description = "감사 로그 생성 확인용",
            )

        // when
        val result = registerService.register(command)

        // then - 감사 로그가 생성되었는지 확인
        transactionApplier.applyPrimaryTransaction {
            val audits = storeAuditRepository.findByStoreIdOrderByRecordedAtDesc(UUID.fromString(result.storeId))

            assertThat(audits).hasSize(1)

            val audit = audits[0]
            assertThat(audit.storeId.toString()).isEqualTo(result.storeId)
            assertThat(audit.eventType).isEqualTo(StoreAuditEventType.REGISTERED)
            assertThat(audit.statusSnapshot).isEqualTo(StoreStatus.REGISTERED)
            assertThat(audit.actorUserId).isEqualTo(OWNER_USER_ID_5)
            assertThat(audit.changeSummary).isEqualTo("스토어 '감사로그 테스트 스토어' 생성됨")
            assertThat(audit.metadata).isNotNull
            assertThat(audit.metadata!!["storeName"]).isEqualTo("감사로그 테스트 스토어")
            assertThat(audit.metadata!!["description"]).isEqualTo("감사 로그 생성 확인용")
            assertThat(audit.metadata!!["createdBy"]).isEqualTo(OWNER_USER_ID_5.toString())
            assertThat(audit.recordedAt).isNotNull
        }
    }
}
