package com.groom.store.domain.service

import com.groom.store.domain.model.UserInfo
import java.util.UUID

interface UserReader {
    fun get(userId: UUID): UserInfo
}
