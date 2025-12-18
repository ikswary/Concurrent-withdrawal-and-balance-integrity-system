package com.wallet.withdrawal.domain.exception

/**
 * 중복 거래 예외
 */
class DuplicateTransactionException(
    val transactionId: String
) : RuntimeException("Duplicate transaction: transactionId=$transactionId")
