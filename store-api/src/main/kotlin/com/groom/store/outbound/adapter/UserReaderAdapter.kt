package com.groom.store.outbound.adapter

import com.groom.store.domain.model.UserInfo
import com.groom.store.domain.service.UserReader
import com.groom.store.outbound.client.UserServiceClient
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserReaderAdapter(
    private val userServiceClient: UserServiceClient,
) : UserReader {
    override fun get(userId: UUID): UserInfo {
        val response = userServiceClient.get(userId)
        return UserInfo(
            id = response.id,
            name = response.name,
            role = response.role,
        )
    }
}
