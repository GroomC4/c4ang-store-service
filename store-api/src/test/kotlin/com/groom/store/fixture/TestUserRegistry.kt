package com.groom.store.fixture

import com.groom.store.domain.model.UserRole
import java.util.UUID

/**
 * 통합 테스트용 사용자 데이터 레지스트리
 *
 * 테스트에서 사용할 사용자 ID와 역할을 중앙에서 관리합니다.
 */
object TestUserRegistry {
    // OWNER 사용자들
    val OWNER_USER_1 =
        TestUser(
            id = UUID.fromString("aaaaaaaa-1111-2222-3333-444444444441"),
            name = "Owner User 1",
            role = UserRole.OWNER,
        )

    val OWNER_USER_2 =
        TestUser(
            id = UUID.fromString("aaaaaaaa-1111-2222-3333-444444444442"),
            name = "Owner User 2",
            role = UserRole.OWNER,
        )

    // CUSTOMER 사용자들
    val CUSTOMER_USER_1 =
        TestUser(
            id = UUID.fromString("bbbbbbbb-1111-2222-3333-444444444441"),
            name = "Customer User 1",
            role = UserRole.CUSTOMER,
        )

    val CUSTOMER_USER_2 =
        TestUser(
            id = UUID.fromString("bbbbbbbb-1111-2222-3333-444444444442"),
            name = "Customer User 2",
            role = UserRole.CUSTOMER,
        )

    // Update 테스트용 사용자들
    val UPDATE_OWNER_USER_1 =
        TestUser(
            id = UUID.fromString("11111111-2222-3333-4444-555555555551"),
            name = "Update Owner User 1",
            role = UserRole.OWNER,
        )

    val UPDATE_OWNER_USER_2 =
        TestUser(
            id = UUID.fromString("22222222-2222-3333-4444-555555555552"),
            name = "Update Owner User 2",
            role = UserRole.OWNER,
        )

    val UPDATE_OWNER_USER_3 =
        TestUser(
            id = UUID.fromString("33333333-2222-3333-4444-555555555553"),
            name = "Update Owner User 3",
            role = UserRole.OWNER,
        )

    // DeleteStoreServiceIntegrationTest용 사용자들
    val DELETE_OWNER_USER_1 =
        TestUser(
            id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            name = "Delete Owner User 1",
            role = UserRole.OWNER,
        )

    val DELETE_OWNER_USER_2 =
        TestUser(
            id = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            name = "Delete Owner User 2",
            role = UserRole.OWNER,
        )

    val DELETE_OWNER_USER_3 =
        TestUser(
            id = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            name = "Delete Owner User 3",
            role = UserRole.OWNER,
        )

    val DELETE_OWNER_USER_4 =
        TestUser(
            id = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
            name = "Delete Owner User 4",
            role = UserRole.OWNER,
        )

    val DELETE_OWNER_USER_5 =
        TestUser(
            id = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
            name = "Delete Owner User 5",
            role = UserRole.OWNER,
        )

    // RegisterStoreServiceIntegrationTest용 추가 사용자들
    val REGISTER_OWNER_USER_2 =
        TestUser(
            id = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            name = "Register Owner User 2",
            role = UserRole.OWNER,
        )

    val REGISTER_OWNER_USER_5 =
        TestUser(
            id = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
            name = "Register Owner User 5",
            role = UserRole.OWNER,
        )

    // UpdateStoreServiceIntegrationTest용 사용자들은 DELETE와 동일한 UUID 사용

    // 전체 사용자 맵
    private val userMap: Map<UUID, TestUser> =
        listOf(
            OWNER_USER_1,
            OWNER_USER_2,
            CUSTOMER_USER_1,
            CUSTOMER_USER_2,
            UPDATE_OWNER_USER_1,
            UPDATE_OWNER_USER_2,
            UPDATE_OWNER_USER_3,
            DELETE_OWNER_USER_1,
            DELETE_OWNER_USER_2,
            DELETE_OWNER_USER_3,
            DELETE_OWNER_USER_4,
            DELETE_OWNER_USER_5,
            REGISTER_OWNER_USER_2,
            REGISTER_OWNER_USER_5,
        ).associateBy { it.id }

    /**
     * 사용자 ID로 테스트 사용자 조회
     * 등록되지 않은 ID는 기본 OWNER 사용자 반환
     */
    fun getUser(userId: UUID): TestUser =
        userMap[userId] ?: TestUser(
            id = userId,
            name = "Unknown User",
            role = UserRole.OWNER, // 기본값
        )

    /**
     * 사용자 ID가 등록된 테스트 사용자인지 확인
     */
    fun isRegisteredUser(userId: UUID): Boolean = userMap.containsKey(userId)

    data class TestUser(
        val id: UUID,
        val name: String,
        val role: UserRole,
    )
}
