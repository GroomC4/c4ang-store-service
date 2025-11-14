package com.groom.store.common.enums

/**
 * 스토어 상태
 */
enum class StoreStatus {
    /**
     * 등록됨 (정상 운영 중)
     */
    REGISTERED,

    /**
     * 정지됨 (관리자에 의해 일시 정지)
     */
    SUSPENDED,

    /**
     * 삭제됨 (소프트 삭제)
     */
    DELETED,
}
