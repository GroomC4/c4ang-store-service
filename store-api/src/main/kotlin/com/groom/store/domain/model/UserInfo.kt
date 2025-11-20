package com.groom.store.domain.model

import java.util.UUID

data class UserInfo(
    val id: UUID,
    val name: String,
    val role: UserRole,
)
