package com.wallet.withdrawal.controller

import com.wallet.withdrawal.service.wallet.dto.BalanceResponse
import com.wallet.withdrawal.service.wallet.dto.WithdrawalRequest
import com.wallet.withdrawal.service.wallet.dto.WithdrawalResponse
import com.wallet.withdrawal.service.wallet.WalletService
import org.springframework.web.bind.annotation.*

/**
 * Wallet Controller
 * 월렛 출금 및 잔액 조회 REST API
 */
@RestController
@RequestMapping("/api/wallets")
class WalletController(
    private val walletService: WalletService
) {

    /**
     * 출금 API
     * POST /api/wallets/{walletId}/withdraw
     */
    @PostMapping("/{walletId}/withdraw")
    fun withdraw(
        @PathVariable walletId: Long,
        @RequestBody request: WithdrawalRequest
    ): WithdrawalResponse {
        return walletService.withdraw(
            walletId = walletId,
            transactionId = request.transactionId,
            amount = request.amount.amount
        )
    }

    /**
     * 잔액 조회 API
     * GET /api/wallets/{walletId}/balance
     */
    @GetMapping("/{walletId}/balance")
    fun getBalance(@PathVariable walletId: Long): BalanceResponse {
        return walletService.getBalance(walletId)
    }
}
