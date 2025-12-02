package com.groom.store.adapter.inbound.web

import com.groom.store.adapter.inbound.web.dto.GetStoreResponse
import com.groom.store.application.dto.GetStoreQuery
import com.groom.store.application.service.GetStoreService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * 스토어 내부 API 컨트롤러.
 *
 * 다른 마이크로서비스에서 호출하는 내부 API입니다.
 * Istio 서비스 메시 내부에서만 접근 가능하며, 인증 헤더 검증을 수행하지 않습니다.
 */
@Tag(name = "Store Internal API", description = "스토어 내부 API (서비스 간 통신용)")
@RestController
@RequestMapping("/internal/v1/stores")
class StoreInternalController(
    private val getStoreService: GetStoreService,
) {
    /**
     * 스토어 ID로 스토어 정보를 조회한다.
     *
     * @param storeId 스토어 ID
     * @return 스토어 상세 정보
     */
    @Operation(summary = "스토어 ID로 조회", description = "스토어 ID로 스토어 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "스토어 조회 성공"),
            ApiResponse(responseCode = "404", description = "스토어를 찾을 수 없음"),
        ],
    )
    @GetMapping("/{storeId}")
    fun getStoreById(
        @PathVariable storeId: UUID,
    ): GetStoreResponse {
        val query = GetStoreQuery(storeId = storeId)
        val result = getStoreService.getStore(query)
        return GetStoreResponse.from(result)
    }

    /**
     * 소유자 ID로 스토어 정보를 조회한다.
     *
     * @param ownerUserId 소유자 사용자 ID
     * @return 스토어 상세 정보
     */
    @Operation(summary = "소유자 ID로 스토어 조회", description = "소유자 사용자 ID로 스토어 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "스토어 조회 성공"),
            ApiResponse(responseCode = "404", description = "스토어를 찾을 수 없음"),
        ],
    )
    @GetMapping("/owner/{ownerUserId}")
    fun getStoreByOwnerId(
        @PathVariable ownerUserId: UUID,
    ): GetStoreResponse {
        val result = getStoreService.getMyStore(ownerUserId)
        return GetStoreResponse.from(result)
    }
}
