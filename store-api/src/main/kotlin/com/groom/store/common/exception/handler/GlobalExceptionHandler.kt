package com.groom.store.common.exception.handler

import com.groom.store.common.exception.ErrorCode
import com.groom.store.common.exception.StoreException
import com.groom.store.common.exception.UserException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(value = [IllegalArgumentException::class])
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        ResponseEntity(
            ErrorResponse(
                code = ErrorCode.INVALID_REQUEST_PARAMETER,
                message = e.message ?: "잘못된 요청입니다.",
            ),
            HttpStatus.BAD_REQUEST,
        )

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<ErrorResponse> =
        ResponseEntity(
            ErrorResponse(
                code = ErrorCode.INVALID_REQUEST_STATE,
                message = e.message ?: "요청을 처리하는데 실패하였습니다.",
            ),
            HttpStatus.INTERNAL_SERVER_ERROR,
        )

    /**
     * 사용자 관련 예외 처리
     *
     * 발생 위치:
     * - SellerPolicy.checkUserIsSeller(): InsufficientPermission
     * - LoadUserPort.loadUser(): UserNotFound
     */
    @ExceptionHandler(UserException::class)
    fun handleUserException(e: UserException): ResponseEntity<ErrorResponse> {
        val (errorCode, httpStatus) =
            when (e) {
                is UserException.DuplicateEmail -> {
                    logger.warn(e) { "Duplicate email: ${e.email}" }
                    ErrorCode.DUPLICATE_EMAIL to HttpStatus.CONFLICT
                }
                is UserException.UserNotFound -> {
                    logger.warn(e) { "User not found: ${e.userId}" }
                    ErrorCode.USER_NOT_FOUND to HttpStatus.NOT_FOUND
                }
                is UserException.UserAlreadyExists -> {
                    logger.warn(e) { "User already exists: ${e.identifier}" }
                    ErrorCode.USER_ALREADY_EXISTS to HttpStatus.CONFLICT
                }
                is UserException.InsufficientPermission -> {
                    logger.warn(e) { "Insufficient permission: userId=${e.userId}, required=${e.requiredRole}, current=${e.currentRole}" }
                    ErrorCode.INSUFFICIENT_PERMISSION to HttpStatus.FORBIDDEN
                }
            }

        return ResponseEntity(
            ErrorResponse(
                code = errorCode,
                message = e.message ?: "사용자 처리 중 오류가 발생하였습니다.",
            ),
            httpStatus,
        )
    }

    @ExceptionHandler(StoreException::class)
    fun handleStoreException(e: StoreException): ResponseEntity<ErrorResponse> {
        val (errorCode, httpStatus) =
            when (e) {
                is StoreException.StoreNotFound -> {
                    logger.warn(e) { "Store not found: storeId=${e.storeId}" }
                    ErrorCode.STORE_NOT_FOUND to HttpStatus.NOT_FOUND
                }
                is StoreException.DuplicateStore -> {
                    logger.warn(e) { "Duplicate store: ownerUserId=${e.ownerUserId}" }
                    ErrorCode.DUPLICATE_STORE to HttpStatus.CONFLICT
                }
                is StoreException.StoreRegistrationRequired -> {
                    logger.warn(e) { "Store registration required: userId=${e.userId}, storeId=${e.storeId}" }
                    ErrorCode.STORE_REGISTRATION_REQUIRED to HttpStatus.BAD_REQUEST
                }
                is StoreException.StoreAccessDenied -> {
                    logger.warn(e) { "Store access denied: storeId=${e.storeId}, userId=${e.userId}" }
                    ErrorCode.STORE_ACCESS_DENIED to HttpStatus.FORBIDDEN
                }
                is StoreException.StoreAlreadySuspended -> {
                    logger.warn(e) { "Store already suspended: storeId=${e.storeId}" }
                    ErrorCode.STORE_ALREADY_SUSPENDED to HttpStatus.CONFLICT
                }
                is StoreException.StoreAlreadyDeleted -> {
                    logger.warn(e) { "Store already deleted: storeId=${e.storeId}" }
                    ErrorCode.STORE_ALREADY_DELETED to HttpStatus.CONFLICT
                }
                is StoreException.CannotUpdateSuspendedStore -> {
                    logger.warn(e) { "Cannot update suspended store: storeId=${e.storeId}" }
                    ErrorCode.CANNOT_UPDATE_SUSPENDED_STORE to HttpStatus.CONFLICT
                }
                is StoreException.CannotUpdateDeletedStore -> {
                    logger.warn(e) { "Cannot update deleted store: storeId=${e.storeId}" }
                    ErrorCode.CANNOT_UPDATE_DELETED_STORE to HttpStatus.CONFLICT
                }
                is StoreException.InvalidStoreStatusTransition -> {
                    logger.warn(
                        e,
                    ) { "Invalid store status transition: storeId=${e.storeId}, from=${e.currentStatus}, to=${e.targetStatus}" }
                    ErrorCode.INVALID_STORE_STATUS_TRANSITION to HttpStatus.CONFLICT
                }
            }

        return ResponseEntity(
            ErrorResponse(
                code = errorCode,
                message = e.message ?: "스토어 처리 중 오류가 발생하였습니다.",
            ),
            httpStatus,
        )
    }
}
