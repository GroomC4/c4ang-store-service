package com.groom.store.domain.model

import com.groom.store.adapter.out.client.UserRole
import java.util.UUID

data class UserInfo(
    val id: UUID,
    val name: String,
    val role: UserRole,
)
