package com.groom.store.domain.service

import com.groom.store.common.enums.StoreStatus
import com.groom.store.common.exception.StoreException
import com.groom.store.domain.model.Store
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Store 도메인의 복잡한 비즈니스 로직을 처리하는 도메인 서비스.
 *
 * 도메인 서비스는 다음과 같은 경우에 사용됩니다:
 * - 여러 엔티티가 협력하는 복잡한 로직
 * - 단일 엔티티에 귀속시키기 애매한 도메인 로직
 * - 도메인 정책을 조율하는 로직
 *
 * Note: Application Service와 구분하기 위해 postfix로 'Service'를 사용하지 않습니다.
 */
@Component
class StoreManager(
    private val storePolicy: StorePolicy,
) {
    /**
     * 스토어 정보를 업데이트합니다.
     *
     * 비즈니스 규칙:
     * - 스토어 소유자만 수정 가능
     * - 일시정지 상태(SUSPENDED)의 스토어는 수정 불가
     * - 삭제된 스토어는 수정 불가
     *
     * @param store 수정할 스토어
     * @param newName 새로운 스토어 이름
     * @param newDescription 새로운 스토어 설명
     * @param requestUserId 요청한 사용자 ID
     * @return 수정된 Store 인스턴스와 도메인 이벤트
     * @throws StoreException.StoreAccessDenied 스토어 소유자가 아닌 경우
     * @throws StoreException.CannotUpdateSuspendedStore 일시정지된 스토어인 경우
     * @throws StoreException.CannotUpdateDeletedStore 삭제된 스토어인 경우
     */
    fun updateStoreInfo(
        store: Store,
        newName: String,
        newDescription: String?,
        requestUserId: UUID,
    ): Store.UpdateInfoResult {
        // 접근 권한 검증
        storePolicy.checkStoreAccess(store.id, requestUserId)

        // 비즈니스 규칙 검증
        when (store.status) {
            StoreStatus.SUSPENDED -> throw StoreException.CannotUpdateSuspendedStore(store.id!!)
            StoreStatus.DELETED -> throw StoreException.CannotUpdateDeletedStore(store.id!!)
            else -> {} // REGISTERED, HIDDEN 상태는 수정 가능
        }

        // 스토어 정보 수정 및 결과 반환 (불변 객체 패턴)
        return store.updateInfo(newName, newDescription)
    }

    /**
     * 스토어를 삭제합니다.
     *
     * 비즈니스 규칙:
     * - 스토어 소유자만 삭제 가능
     * - 이미 삭제된 스토어는 재삭제 불가
     *
     * @param store 삭제할 스토어
     * @param requestUserId 요청한 사용자 ID
     * @return 삭제된 Store 인스턴스와 도메인 이벤트
     * @throws StoreException.StoreAccessDenied 스토어 소유자가 아닌 경우
     * @throws StoreException.StoreAlreadyDeleted 이미 삭제된 스토어인 경우
     */
    fun deleteStore(
        store: Store,
        requestUserId: UUID,
    ): Store.DeleteResult {
        // 접근 권한 검증
        storePolicy.checkStoreAccess(store.id, requestUserId)

        // 스토어 삭제 및 결과 반환 (불변 객체 패턴)
        return store.delete()
    }
}
