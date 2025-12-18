package com.wallet.withdrawal.domain.exception

import org.springframework.http.HttpStatus

/**
 * Base Wallet Exception
 * HTTP 상태 코드를 포함하는 기본 예외 클래스
 */
abstract class CommonException(
    message: String,
    val httpStatus: HttpStatus
) : RuntimeException(message)
