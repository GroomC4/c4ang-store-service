package com.groom.store.common.exception

import java.util.UUID

/**
 * 도메인 예외의 최상위 sealed class
 *
 * 모든 비즈니스 예외는 이 클래스를 상속받아 정의됩니다.
 * sealed class를 사용하여 컴파일 타임에 모든 예외 타입을 exhaustive하게 처리할 수 있습니다.
 */
sealed class DomainException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * 사용자 관련 예외
 */
sealed class UserException(
    message: String,
    cause: Throwable? = null,
) : DomainException(message, cause) {
    /**
     * 이메일이 중복된 경우
     * @param email 중복된 이메일
     */
    data class DuplicateEmail(
        val email: String,
    ) : UserException("이미 존재하는 이메일입니다: $email")

    /**
     * 사용자를 찾을 수 없는 경우
     * @param userId 찾을 수 없는 사용자 ID
     */
    data class UserNotFound(
        val userId: UUID,
    ) : UserException("사용자를 찾을 수 없습니다: $userId")

    /**
     * 사용자가 이미 존재하는 경우
     * @param identifier 중복된 식별자
     */
    data class UserAlreadyExists(
        val identifier: String,
    ) : UserException("사용자가 이미 존재합니다: $identifier")

    /**
     * 작업을 수행할 권한이 부족한 경우
     * @param userId 사용자 ID
     * @param requiredRole 필요한 역할
     * @param currentRole 현재 역할
     */
    data class InsufficientPermission(
        val userId: UUID,
        val requiredRole: String,
        val currentRole: String,
    ) : UserException("이 작업을 수행할 권한이 없습니다.")
}

/**
 * 스토어 관련 예외
 */
sealed class StoreException(
    message: String,
    cause: Throwable? = null,
) : DomainException(message, cause) {
    /**
     * 스토어를 찾을 수 없는 경우
     * @param storeId 찾을 수 없는 스토어 ID
     */
    data class StoreNotFound(
        val storeId: UUID,
    ) : StoreException("스토어를 찾을 수 없습니다: $storeId")

    /**
     * 판매자가 이미 스토어를 가지고 있는 경우
     * @param ownerUserId 판매자 사용자 ID
     */
    data class DuplicateStore(
        val ownerUserId: UUID,
    ) : StoreException("이미 스토어가 존재합니다. 판매자당 하나의 스토어만 운영할 수 있습니다")

    /**
     * 상품 등록 시 스토어가 없는 경우
     * @param storeId 찾을 수 없는 스토어 ID
     */
    data class StoreRegistrationRequired(
        val userId: UUID,
        val storeId: UUID,
    ) : StoreException("스토어를 찾을 수 없습니다. 상품을 등록하기 전에 먼저 스토어를 등록해주세요. (storeId: $storeId)")

    /**
     * 스토어 접근 권한이 없는 경우
     * @param storeId 스토어 ID
     * @param userId 사용자 ID
     */
    data class StoreAccessDenied(
        val storeId: UUID,
        val userId: UUID,
    ) : StoreException("스토어에 대한 접근 권한이 없습니다")

    /**
     * 이미 정지된 스토어를 정지하려는 경우
     * @param storeId 스토어 ID
     */
    data class StoreAlreadySuspended(
        val storeId: UUID,
    ) : StoreException("이미 정지된 스토어입니다")

    /**
     * 이미 삭제된 스토어를 삭제하려는 경우
     * @param storeId 스토어 ID
     */
    data class StoreAlreadyDeleted(
        val storeId: UUID,
    ) : StoreException("이미 삭제된 스토어입니다")

    /**
     * 일시정지된 스토어를 수정하려는 경우
     * @param storeId 스토어 ID
     */
    data class CannotUpdateSuspendedStore(
        val storeId: UUID,
    ) : StoreException("일시정지된 스토어는 수정할 수 없습니다")

    /**
     * 삭제된 스토어를 수정하려는 경우
     * @param storeId 스토어 ID
     */
    data class CannotUpdateDeletedStore(
        val storeId: UUID,
    ) : StoreException("삭제된 스토어는 수정할 수 없습니다")

    /**
     * 스토어 상태 변경이 불가능한 경우
     * @param storeId 스토어 ID
     * @param currentStatus 현재 상태
     * @param targetStatus 변경하려는 상태
     */
    data class InvalidStoreStatusTransition(
        val storeId: UUID,
        val currentStatus: String,
        val targetStatus: String,
    ) : StoreException("스토어 상태를 $currentStatus 에서 $targetStatus 로 변경할 수 없습니다")
}
