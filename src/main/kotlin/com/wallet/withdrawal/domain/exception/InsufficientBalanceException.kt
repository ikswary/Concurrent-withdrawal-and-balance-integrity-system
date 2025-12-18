package com.wallet.withdrawal.domain.exception

import org.springframework.http.HttpStatus
import java.math.BigDecimal

/**
 * 잔액 부족 예외
 * HTTP 400 Bad Request
 */
class InsufficientBalanceException(
    val currentBalance: BigDecimal,
    val requestedAmount: BigDecimal
) : CommonException(
    message = "Insufficient balance: current=$currentBalance, requested=$requestedAmount",
    httpStatus = HttpStatus.BAD_REQUEST
)
