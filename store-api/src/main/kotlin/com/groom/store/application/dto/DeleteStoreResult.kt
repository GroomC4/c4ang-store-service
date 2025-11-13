package com.groom.ecommerce.store.application.dto

import java.time.LocalDateTime

/**
 * 스토어 삭제 결과.
 *
 * @property storeId 삭제된 스토어 ID
 * @property ownerUserId 스토어 소유자 ID
 * @property name 스토어명
 * @property deletedAt 삭제 시각
 */
data class DeleteStoreResult(
    val storeId: String,
    val ownerUserId: String,
    val name: String,
    val deletedAt: LocalDateTime,
)
