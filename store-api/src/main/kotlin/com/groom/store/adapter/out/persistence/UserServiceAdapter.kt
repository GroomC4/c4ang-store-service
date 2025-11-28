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

        return UserInfo(
            id = UUID.fromString(response.userId),
            name = response.profile.fullName.ifBlank { response.username },
            role = UserRole.fromString(response.role),
        )
    }
}
