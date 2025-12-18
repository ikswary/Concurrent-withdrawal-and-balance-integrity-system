package com.wallet.withdrawal.domain.exception

import org.springframework.http.HttpStatus

/**
 * 중복 거래 예외
 * HTTP 409 Conflict
 */
class DuplicateTransactionException(
    val transactionId: String
) : CommonException(
    message = "Duplicate transaction: transactionId=$transactionId",
    httpStatus = HttpStatus.CONFLICT
)
