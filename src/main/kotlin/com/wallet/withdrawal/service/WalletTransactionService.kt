package com.wallet.withdrawal.service

import com.wallet.withdrawal.domain.TransactionHistory
import com.wallet.withdrawal.domain.exception.InsufficientBalanceException
import com.wallet.withdrawal.domain.exception.WalletNotFoundException
import com.wallet.withdrawal.dto.WithdrawalResponse
import com.wallet.withdrawal.repository.TransactionHistoryRepository
import com.wallet.withdrawal.repository.WalletRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import org.slf4j.LoggerFactory

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
        // 월렛 조회
        val wallet = walletRepository.findById(walletId)
            .orElseThrow { WalletNotFoundException(walletId) }

        // 잔액 검증
        if (wallet.balance < amount) {
            throw InsufficientBalanceException(wallet.balance, amount)
        }

        // 잔액 차감
        wallet.balance = wallet.balance.subtract(amount)
        walletRepository.save(wallet)

        // 거래 내역 저장
        val history = TransactionHistory(
            wallet = wallet,
            transactionId = transactionId,
            withdrawalAmount = amount,
            remainingBalance = wallet.balance
        )
        transactionHistoryRepository.save(history)

        return WithdrawalResponse.from(history)
    }
}
