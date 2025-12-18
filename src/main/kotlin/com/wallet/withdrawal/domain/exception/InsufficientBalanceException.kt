package com.wallet.withdrawal.domain.exception

import java.math.BigDecimal

/**
 * 잔액 부족 예외
 */
class InsufficientBalanceException(
    val currentBalance: BigDecimal,
    val requestedAmount: BigDecimal
) : RuntimeException("Insufficient balance: current=$currentBalance, requested=$requestedAmount")
