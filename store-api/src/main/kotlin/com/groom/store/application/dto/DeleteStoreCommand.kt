package com.groom.ecommerce.store.application.dto

import java.util.UUID

/**
 * 스토어 삭제 커맨드.
 *
 * @property storeId 삭제할 스토어 ID
 * @property userId 요청한 사용자 ID
 */
data class DeleteStoreCommand(
    val storeId: UUID,
    val userId: UUID,
)
