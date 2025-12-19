package com.wallet.withdrawal.service.wallet

import com.wallet.withdrawal.domain.TransactionHistory
import com.wallet.withdrawal.domain.exception.InsufficientBalanceException
import com.wallet.withdrawal.domain.exception.WalletNotFoundException
import com.wallet.withdrawal.domain.vo.Money
import com.wallet.withdrawal.service.wallet.dto.WithdrawalResponse
import com.wallet.withdrawal.repository.TransactionHistoryRepository
import com.wallet.withdrawal.repository.WalletRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * Wallet Transaction Service
 * 월렛 트랜잭션 실행 (DB 작업)
 */
@Service
class WalletTransactionService(
    private val walletRepository: WalletRepository,
    private val transactionHistoryRepository: TransactionHistoryRepository
) {

    /**
     * 출금 실행 (트랜잭션 내에서)
     */
    @Transactional
    fun executeWithdrawal(
        walletId: Long,
        transactionId: String,
        amount: BigDecimal
    ): WithdrawalResponse {
        val moneyAmount = Money(amount)

        // 월렛 조회
        val wallet = walletRepository.findById(walletId)
            .orElseThrow { WalletNotFoundException(walletId) }

        // 잔액 검증
        if (wallet.balance < moneyAmount) {
            throw InsufficientBalanceException(wallet.balance.amount, moneyAmount.amount)
        }

        // 잔액 차감
        wallet.balance = wallet.balance - moneyAmount
        walletRepository.save(wallet)

        // 거래 내역 저장
        val history = TransactionHistory(
            wallet = wallet,
            transactionId = transactionId,
            withdrawalAmount = moneyAmount,
            remainingBalance = wallet.balance
        )
        transactionHistoryRepository.save(history)

        return WithdrawalResponse.Companion.from(history)
    }
}