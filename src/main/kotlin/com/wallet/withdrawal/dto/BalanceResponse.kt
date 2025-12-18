package com.wallet.withdrawal.dto

import com.wallet.withdrawal.domain.Wallet
import com.wallet.withdrawal.domain.vo.Money
import java.time.LocalDateTime

/**
 * Balance Response DTO
 */
data class BalanceResponse(
    val walletId: Long,
    val balance: Money,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(wallet: Wallet): BalanceResponse {
            return BalanceResponse(
                walletId = wallet.id!!,
                balance = wallet.balance,
                updatedAt = wallet.updatedAt
            )
        }
    }
}
