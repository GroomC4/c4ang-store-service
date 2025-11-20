package com.groom.store.adapter.out.persistence

import com.groom.store.adapter.out.client.UserServiceClient
import com.groom.store.domain.model.UserInfo
import com.groom.store.domain.model.UserRole
import com.groom.store.domain.port.LoadUserPort
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * 사용자 서비스 Adapter.
 * Domain Port를 구현하고, Feign Client를 사용하여 User Service와 통신합니다.
 * c4ang-contract-hub의 Avro 스키마로 생성된 UserInternalResponse를 도메인 모델로 변환합니다.
 *
 * 외부 API의 UserRole을 도메인 UserRole로 매핑하여
 * 도메인 레이어를 외부 변경으로부터 보호합니다.
 */
@Component
class UserServiceAdapter(
    private val userServiceClient: UserServiceClient,
) : LoadUserPort {
    override fun loadById(userId: UUID): UserInfo {
        val response = userServiceClient.get(userId)

        // UserInternalResponse (Contract 생성 클래스) -> UserInfo (도메인 모델) 변환
        // API 명세에 따르면:
        // - userId: 사용자 고유 ID
        // - username or fullName: 사용자 이름 (profile.fullName 사용)
        // - role: 사용자 역할 (Contract UserRole -> 도메인 UserRole 매핑)
        return UserInfo(
            id = UUID.fromString(response.getUserId()),
            name = response.getProfile()?.getFullName() ?: response.getUsername(),
            role = UserRole.fromContractRole(response.getRole()),
        )
    }
}
