package com.groom.store.adapter.inbound.web

import com.groom.store.adapter.inbound.web.dto.DeleteStoreResponse
import com.groom.store.adapter.inbound.web.dto.GetStoreResponse
import com.groom.store.adapter.inbound.web.dto.RegisterStoreRequest
import com.groom.store.adapter.inbound.web.dto.RegisterStoreResponse
import com.groom.store.adapter.inbound.web.dto.UpdateStoreRequest
import com.groom.store.adapter.inbound.web.dto.UpdateStoreResponse
import com.groom.store.application.dto.DeleteStoreCommand
import com.groom.store.application.dto.GetStoreQuery
import com.groom.store.application.service.DeleteStoreService
import com.groom.store.application.service.GetStoreService
import com.groom.store.application.service.RegisterService
import com.groom.store.application.service.UpdateService
import com.groom.store.common.util.IstioHeaderExtractor
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * 스토어 관리 REST API 컨트롤러.
 */
@Tag(name = "Store Management", description = "스토어 관리 API")
@RestController
@RequestMapping("/api/v1/stores")
class StoreController(
    private val registerService: RegisterService,
    private val updateService: UpdateService,
    private val deleteStoreService: DeleteStoreService,
    private val getStoreService: GetStoreService,
    private val istioHeaderExtractor: IstioHeaderExtractor,
) {
    /**
     * 스토어를 등록한다.
     * 인증된 Owner만 스토어를 등록할 수 있다.
     *
     * Istio API Gateway가 JWT 검증 후 X-User-Id 헤더로 사용자 ID를 주입하며,
     * 추가로 애플리케이션 계층(StorePolicy)에서 DB 기반 역할 검증을 수행한다.
     *
     * @param httpRequest HTTP 요청 (Istio 헤더 추출용)
     * @param request 스토어 등록 요청
     * @return 등록된 스토어 정보
     */
    @Operation(summary = "스토어 등록", description = "새로운 스토어를 등록합니다. Owner 권한이 필요합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "스토어 등록 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
            ApiResponse(responseCode = "403", description = "권한 없음 (Owner 권한 필요)"),
        ],
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun registerStore(
        httpRequest: HttpServletRequest,
        @RequestBody request: RegisterStoreRequest,
    ): RegisterStoreResponse {
        val userId = istioHeaderExtractor.extractUserId(httpRequest)
        val command = request.toCommand(userId)
        val result = registerService.register(command)
        return RegisterStoreResponse.from(result)
    }

    /**
     * 스토어 정보를 수정한다.
     * 인증된 Owner만 자신의 스토어를 수정할 수 있다.
     *
     * @param storeId 스토어 ID
     * @param request 스토어 수정 요청
     * @return 수정된 스토어 정보
     */
    @Operation(summary = "스토어 정보 수정", description = "스토어 정보를 수정합니다. Owner 권한이 필요하며, 자신의 스토어만 수정 가능합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "스토어 수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
            ApiResponse(responseCode = "403", description = "권한 없음 (본인 스토어만 수정 가능)"),
            ApiResponse(responseCode = "404", description = "스토어를 찾을 수 없음"),
        ],
    )
    @PatchMapping("/{storeId}")
    fun updateStore(
        httpRequest: HttpServletRequest,
        @PathVariable storeId: UUID,
        @RequestBody request: UpdateStoreRequest,
    ): UpdateStoreResponse {
        val userId = istioHeaderExtractor.extractUserId(httpRequest)
        val command = request.toCommand(storeId, userId)
        val result = updateService.update(command)
        return UpdateStoreResponse.from(result)
    }

    /**
     * 스토어를 삭제한다 (소프트 삭제).
     * 인증된 Owner만 자신의 스토어를 삭제할 수 있다.
     *
     * @param storeId 스토어 ID
     * @return 삭제된 스토어 정보
     */
    @Operation(summary = "스토어 삭제", description = "스토어를 삭제합니다 (소프트 삭제). Owner 권한이 필요하며, 자신의 스토어만 삭제 가능합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "스토어 삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
            ApiResponse(responseCode = "403", description = "권한 없음 (본인 스토어만 삭제 가능)"),
            ApiResponse(responseCode = "404", description = "스토어를 찾을 수 없음"),
        ],
    )
    @DeleteMapping("/{storeId}")
    fun deleteStore(
        httpRequest: HttpServletRequest,
        @PathVariable storeId: UUID,
    ): DeleteStoreResponse {
        val userId = istioHeaderExtractor.extractUserId(httpRequest)
        val command =
            DeleteStoreCommand(
                storeId = storeId,
                userId = userId,
            )
        val result = deleteStoreService.delete(command)
        return DeleteStoreResponse.from(result)
    }

    /**
     * 스토어 상세 정보를 조회한다.
     * 누구나 스토어 상세 정보를 조회할 수 있다.
     *
     * @param storeId 스토어 ID
     * @return 스토어 상세 정보
     */
    @Operation(summary = "스토어 상세 조회", description = "스토어 상세 정보를 조회합니다. 인증 없이 누구나 조회 가능합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "스토어 조회 성공"),
            ApiResponse(responseCode = "404", description = "스토어를 찾을 수 없음"),
        ],
    )
    @GetMapping("/{storeId}")
    fun getStore(
        @PathVariable storeId: UUID,
    ): GetStoreResponse {
        val query = GetStoreQuery(storeId = storeId)
        val result = getStoreService.getStore(query)
        return GetStoreResponse.from(result)
    }

    /**
     * 내 스토어 조회
     *
     * @return 스토어 상세 정보
     */
    @Operation(summary = "내 스토어 조회", description = "판매자인 경우 내 스토어의 정보를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "스토어 조회 성공"),
            ApiResponse(responseCode = "404", description = "스토어를 찾을 수 없음"),
        ],
    )
    @GetMapping("/mine")
    fun getMyStore(httpRequest: HttpServletRequest): GetStoreResponse {
        val userId = istioHeaderExtractor.extractUserId(httpRequest)
        val result = getStoreService.getMyStore(userId)
        return GetStoreResponse.from(result)
    }
}
