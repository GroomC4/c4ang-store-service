package com.groom.store.common.exception

/**
 * API 에러 코드 상수
 *
 * 클라이언트가 에러 타입을 프로그래밍적으로 구분할 수 있도록 제공하는 에러 코드입니다.
 * 각 코드는 구체적인 비즈니스 에러 상황을 표현합니다.
 */
object ErrorCode {
    // ========== 사용자 관련 에러 ==========
    const val DUPLICATE_EMAIL = "DUPLICATE_EMAIL" // 이메일 중복
    const val USER_NOT_FOUND = "USER_NOT_FOUND" // 사용자 없음
    const val USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS" // 사용자 이미 존재
    const val INSUFFICIENT_PERMISSION = "INSUFFICIENT_PERMISSION" // 권한 부족

    // ========== 스토어 관련 에러 ==========
    const val DUPLICATE_STORE = "DUPLICATE_STORE" // 스토어 중복
    const val STORE_NOT_FOUND = "STORE_NOT_FOUND" // 스토어 없음
    const val STORE_REGISTRATION_REQUIRED = "STORE_REGISTRATION_REQUIRED" // 스토어 등록 필요
    const val STORE_ALREADY_DELETED = "STORE_ALREADY_DELETED" // 스토어 이미 삭제됨
    const val STORE_ALREADY_SUSPENDED = "STORE_ALREADY_SUSPENDED" // 스토어 이미 정지됨
    const val INVALID_STORE_STATUS_TRANSITION = "INVALID_STORE_STATUS_TRANSITION" // 스토어 상태 변경 불가
    const val STORE_ACCESS_DENIED = "STORE_ACCESS_DENIED" // 스토어 접근 권한 없음
    const val CANNOT_UPDATE_SUSPENDED_STORE = "CANNOT_UPDATE_SUSPENDED_STORE" // 일시정지된 스토어 수정 불가
    const val CANNOT_UPDATE_DELETED_STORE = "CANNOT_UPDATE_DELETED_STORE" // 삭제된 스토어 수정 불가

    // ========== 요청 관련 에러 ==========
    const val INVALID_REQUEST_PARAMETER = "INVALID_REQUEST_PARAMETER" // 요청 파라미터 오류
    const val INVALID_REQUEST_STATE = "INVALID_REQUEST_STATE" // 잘못된 요청 상태
}
