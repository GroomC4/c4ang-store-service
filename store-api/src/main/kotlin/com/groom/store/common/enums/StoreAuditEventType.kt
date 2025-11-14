package com.groom.store.common.enums

/**
 * 스토어 감사 이벤트 타입
 * DDL: p_store_audit.event_type 컬럼에 대응
 */
enum class StoreAuditEventType {
    /**
     * 스토어 등록
     */
    REGISTERED,

    /**
     * 스토어 정보 수정
     */
    INFO_UPDATED,

    /**
     * 스토어 정지
     */
    SUSPENDED,

    /**
     * 스토어 삭제
     */
    DELETED,
}
