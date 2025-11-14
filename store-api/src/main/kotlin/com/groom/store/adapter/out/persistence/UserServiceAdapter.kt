package com.groom.store.adapter.out.persistence

import com.groom.store.domain.model.UserInfo
import com.groom.store.domain.port.LoadUserPort
import com.groom.store.outbound.client.UserServiceClient
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * 사용자 서비스 Adapter.
 * Domain Port를 구현하고, Feign Client를 사용하여 User Service와 통신합니다.
 */
@Component
class UserServiceAdapter(
    private val userServiceClient: UserServiceClient,
) : LoadUserPort {

    override fun loadById(userId: UUID): UserInfo {
        val response = userServiceClient.get(userId)
        return UserInfo(
            id = response.id,
            name = response.name,
            role = response.role,
        )
    }
}
