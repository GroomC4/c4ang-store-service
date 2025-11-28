package com.groom.store.domain.model

/**
 * 도메인 레이어의 UserRole enum
 */
enum class UserRole {
    CUSTOMER,
    OWNER,
    MANAGER,
    MASTER;

    companion object {
        fun fromString(role: String): UserRole {
            return when (role.uppercase()) {
                "CUSTOMER" -> CUSTOMER
                "OWNER" -> OWNER
                "MANAGER" -> MANAGER
                "MASTER" -> MASTER
                else -> CUSTOMER
            }
        }
    }
}