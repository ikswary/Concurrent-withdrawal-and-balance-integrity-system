package com.wallet.withdrawal.config

import com.wallet.withdrawal.domain.exception.CommonException
import com.wallet.withdrawal.service.wallet.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Global Exception Handler
 * 전역 예외 처리
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * WalletException 처리
     * 각 예외가 정의한 HTTP 상태 코드를 사용
     */
    @ExceptionHandler(CommonException::class)
    fun handleWalletException(
        ex: CommonException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = ex.httpStatus.value(),
            error = ex.httpStatus.reasonPhrase,
            message = ex.message ?: "An error occurred",
            path = request.requestURI
        )
        return ResponseEntity.status(ex.httpStatus).body(errorResponse)
    }

    /**
     * 일반 예외 처리
     * HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = ex.message ?: "An unexpected error occurred",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}