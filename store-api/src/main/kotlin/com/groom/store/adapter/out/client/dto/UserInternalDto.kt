package com.groom.store.adapter.out.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Internal User API 응답 DTO
 *
 * customer-service의 Internal API 응답과 동일한 구조
 */
data class UserInternalDto(
    @JsonProperty("userId")
    val userId: String,
    @JsonProperty("username")
    val username: String,
    @JsonProperty("email")
    val email: String,
    @JsonProperty("role")
    val role: String,
    @JsonProperty("isActive")
    val isActive: Boolean,
    @JsonProperty("profile")
    val profile: UserProfileDto,
    @JsonProperty("createdAt")
    val createdAt: Long,
    @JsonProperty("updatedAt")
    val updatedAt: Long,
    @JsonProperty("lastLoginAt")
    val lastLoginAt: Long?,
)

data class UserProfileDto(
    @JsonProperty("fullName")
    val fullName: String,
    @JsonProperty("phoneNumber")
    val phoneNumber: String,
    @JsonProperty("address")
    val address: String?,
)
