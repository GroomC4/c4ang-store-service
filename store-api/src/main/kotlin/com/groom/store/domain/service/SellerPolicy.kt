package com.groom.store.domain.service

import com.groom.store.common.exception.UserException
import com.groom.store.domain.port.LoadUserPort
import com.groom.store.outbound.client.UserRole
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SellerPolicy(
    private val loadUserPort: LoadUserPort,
) {
    fun checkOwnerRole(ownerUserId: UUID) {
        val user = loadUserPort.loadById(ownerUserId)
        if (user.role != UserRole.OWNER) {
            throw UserException.InsufficientPermission(
                userId = ownerUserId,
                requiredRole = UserRole.OWNER.name,
                currentRole = user.role.name,
            )
        }
    }
}
