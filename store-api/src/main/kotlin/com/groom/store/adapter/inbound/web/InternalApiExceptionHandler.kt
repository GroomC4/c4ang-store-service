package com.groom.store.adapter.inbound.web

import com.groom.store.adapter.inbound.web.dto.InternalApiErrorResponse
import com.groom.store.common.exception.ErrorCode
import com.groom.store.common.exception.StoreException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Internal API 전용 예외 핸들러.
 *
 * /internal/ 경로에서 발생하는 예외를 처리하여 다른 마이크로서비스에서
 * 기대하는 에러 응답 형식(error, message)으로 반환합니다.
 */
@RestControllerAdvice(assignableTypes = [StoreInternalController::class])
@Order(Ordered.HIGHEST_PRECEDENCE)
class InternalApiExceptionHandler {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(StoreException.StoreNotFound::class)
    fun handleStoreNotFoundException(e: StoreException.StoreNotFound): ResponseEntity<InternalApiErrorResponse> {
        logger.warn(e) { "Store not found: storeId=${e.storeId}" }

        return ResponseEntity(
            InternalApiErrorResponse(
                error = ErrorCode.STORE_NOT_FOUND,
                message = "Store not found with id: ${e.storeId}",
            ),
            HttpStatus.NOT_FOUND,
        )
    }
}
