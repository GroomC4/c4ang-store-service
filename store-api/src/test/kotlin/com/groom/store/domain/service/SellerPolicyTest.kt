package com.groom.store.domain.service

import com.groom.store.common.annotation.UnitTest
import com.groom.store.common.exception.UserException
import com.groom.store.domain.model.UserInfo
import com.groom.store.domain.model.UserRole
import com.groom.store.domain.port.LoadUserPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

/**
 * SellerPolicy 도메인 서비스 단위 테스트
 *
 * 판매자 권한 검증 로직을 테스트합니다.
 */
@UnitTest
@DisplayName("SellerPolicy 단위 테스트")
class SellerPolicyTest {
    private lateinit var loadUserPort: LoadUserPort
    private lateinit var sellerPolicy: SellerPolicy

    @BeforeEach
    fun setUp() {
        loadUserPort = mockk()
        sellerPolicy = SellerPolicy(loadUserPort)
    }

    @Test
    @DisplayName("OWNER 역할을 가진 사용자는 권한 검증을 통과한다")
    fun testCheckOwnerRole_Owner_Success() {
        // given
        val ownerUserId = UUID.randomUUID()
        val ownerUser =
            UserInfo(
                id = ownerUserId,
                name = "Owner User",
                role = UserRole.OWNER,
            )

        every { loadUserPort.loadById(ownerUserId) } returns ownerUser

        // when & then
        assertThatCode {
            sellerPolicy.checkOwnerRole(ownerUserId)
        }.doesNotThrowAnyException()

        verify { loadUserPort.loadById(ownerUserId) }
    }

    @Test
    @DisplayName("CUSTOMER 역할을 가진 사용자는 권한 검증에 실패한다")
    fun testCheckOwnerRole_Customer_ThrowsException() {
        // given
        val customerUserId = UUID.randomUUID()
        val customerUser =
            UserInfo(
                id = customerUserId,
                name = "Customer User",
                role = UserRole.CUSTOMER,
            )

        every { loadUserPort.loadById(customerUserId) } returns customerUser

        // when & then
        val exception =
            assertThrows<UserException.InsufficientPermission> {
                sellerPolicy.checkOwnerRole(customerUserId)
            }

        assertThat(exception.userId).isEqualTo(customerUserId)
        assertThat(exception.requiredRole).isEqualTo(UserRole.OWNER.name)
        assertThat(exception.currentRole).isEqualTo(UserRole.CUSTOMER.name)
        verify { loadUserPort.loadById(customerUserId) }
    }

    @Test
    @DisplayName("MANAGER 역할을 가진 사용자는 권한 검증에 실패한다")
    fun testCheckOwnerRole_Manager_ThrowsException() {
        // given
        val managerUserId = UUID.randomUUID()
        val managerUser =
            UserInfo(
                id = managerUserId,
                name = "Manager User",
                role = UserRole.MANAGER,
            )

        every { loadUserPort.loadById(managerUserId) } returns managerUser

        // when & then
        val exception =
            assertThrows<UserException.InsufficientPermission> {
                sellerPolicy.checkOwnerRole(managerUserId)
            }

        assertThat(exception.userId).isEqualTo(managerUserId)
        assertThat(exception.requiredRole).isEqualTo(UserRole.OWNER.name)
        assertThat(exception.currentRole).isEqualTo(UserRole.MANAGER.name)
        verify { loadUserPort.loadById(managerUserId) }
    }

    @Test
    @DisplayName("MASTER 역할을 가진 사용자는 권한 검증에 실패한다")
    fun testCheckOwnerRole_Master_ThrowsException() {
        // given
        val masterUserId = UUID.randomUUID()
        val masterUser =
            UserInfo(
                id = masterUserId,
                name = "Master User",
                role = UserRole.MASTER,
            )

        every { loadUserPort.loadById(masterUserId) } returns masterUser

        // when & then
        val exception =
            assertThrows<UserException.InsufficientPermission> {
                sellerPolicy.checkOwnerRole(masterUserId)
            }

        assertThat(exception.userId).isEqualTo(masterUserId)
        assertThat(exception.requiredRole).isEqualTo(UserRole.OWNER.name)
        assertThat(exception.currentRole).isEqualTo(UserRole.MASTER.name)
        verify { loadUserPort.loadById(masterUserId) }
    }

    @Test
    @DisplayName("여러 번 호출해도 매번 사용자 정보를 조회한다")
    fun testCheckOwnerRole_MultipleCalls() {
        // given
        val ownerUserId = UUID.randomUUID()
        val ownerUser =
            UserInfo(
                id = ownerUserId,
                name = "Owner User",
                role = UserRole.OWNER,
            )

        every { loadUserPort.loadById(ownerUserId) } returns ownerUser

        // when
        sellerPolicy.checkOwnerRole(ownerUserId)
        sellerPolicy.checkOwnerRole(ownerUserId)
        sellerPolicy.checkOwnerRole(ownerUserId)

        // then
        verify(exactly = 3) { loadUserPort.loadById(ownerUserId) }
    }
}
