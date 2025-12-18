package com.wallet.withdrawal.dto

import com.wallet.withdrawal.domain.vo.Money

/**
 * Withdrawal Request DTO
 */
data class WithdrawalRequest(
    val transactionId: String,
    val amount: Money
)
