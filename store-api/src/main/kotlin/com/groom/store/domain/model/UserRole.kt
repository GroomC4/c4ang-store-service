package com.groom.store.domain.model

/**
 * 도메인 레이어의 UserRole enum
 *
 * 외부 API(c4ang-contract-hub)의 UserRole과 분리하여
 * 도메인 로직을 외부 변경으로부터 보호합니다.
 *
 * Adapter 레이어에서 외부 UserRole을 이 enum으로 매핑합니다.
 */
enum class UserRole {
    CUSTOMER,
    OWNER,
    MANAGER,
    MASTER;

    companion object {
        /**
         * Contract UserRole을 도메인 UserRole로 매핑
         *
         * @param contractRole c4ang-contract-hub의 UserRole
         * @return 도메인 UserRole
         */
        fun fromContractRole(contractRole: com.groom.ecommerce.customer.api.avro.UserRole): UserRole {
            return when (contractRole) {
                com.groom.ecommerce.customer.api.avro.UserRole.CUSTOMER -> CUSTOMER
                com.groom.ecommerce.customer.api.avro.UserRole.OWNER -> OWNER
                // Contract hub에 MANAGER, MASTER가 없으면 기본값으로 처리
                // 향후 contract hub에 추가되면 매핑 로직 수정
                else -> CUSTOMER // 기본값
            }
        }
    }
}