package com.wallet.withdrawal.domain.exception

import org.springframework.http.HttpStatus

/**
 * 월렛을 찾을 수 없음 예외
 * HTTP 404 Not Found
 */
class WalletNotFoundException(
    val walletId: Long
) : CommonException(
    message = "Wallet not found: walletId=$walletId",
    httpStatus = HttpStatus.NOT_FOUND
)
