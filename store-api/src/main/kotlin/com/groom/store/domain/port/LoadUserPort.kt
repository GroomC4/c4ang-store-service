package com.groom.store.domain.port

import com.groom.store.domain.model.UserInfo
import java.util.UUID

/**
 * 사용자 조회를 위한 Outbound Port.
 * Domain이 외부 User Service에 요구하는 계약.
 */
interface LoadUserPort {
    /**
     * 사용자 ID로 사용자 정보를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 조회된 사용자 정보
     * @throws com.groom.store.common.exception.UserException.UserNotFound 사용자를 찾을 수 없는 경우
     */
    fun loadById(userId: UUID): UserInfo
}
