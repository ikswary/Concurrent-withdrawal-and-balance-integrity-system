package com.wallet.withdrawal.dto

import com.wallet.withdrawal.domain.TransactionHistory
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Withdrawal Response DTO
 */
data class WithdrawalResponse(
    val transactionId: String,
    val walletId: Long,
    val withdrawalAmount: BigDecimal,
    val remainingBalance: BigDecimal,
    val transactionTime: LocalDateTime
) {
    companion object {
        fun from(history: TransactionHistory): WithdrawalResponse {
            return WithdrawalResponse(
                transactionId = history.transactionId,
                walletId = history.walletId,
                withdrawalAmount = history.withdrawalAmount,
                remainingBalance = history.remainingBalance,
                transactionTime = history.createdAt
            )
        }
    }
}
