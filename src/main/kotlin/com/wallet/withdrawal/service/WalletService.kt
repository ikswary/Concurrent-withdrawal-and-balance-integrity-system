package com.wallet.withdrawal.service

import com.wallet.withdrawal.domain.exception.WalletNotFoundException
import com.wallet.withdrawal.dto.BalanceResponse
import com.wallet.withdrawal.dto.WithdrawalResponse
import com.wallet.withdrawal.repository.TransactionHistoryRepository
import com.wallet.withdrawal.repository.WalletRepository
import com.wallet.withdrawal.service.lock.LockManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * Wallet Service
 * 월렛 출금 및 잔액 조회 서비스 (락 관리 + 멱등성 체크)
 */
@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val transactionHistoryRepository: TransactionHistoryRepository,
    private val lockManager: LockManager,
    private val walletTransactionService: WalletTransactionService
) {

    /**
     * 출금 처리
     * - 멱등성 보장 (transactionId 중복 체크)
     * - 분산 락을 통한 동시성 제어
     */
    fun withdraw(walletId: Long, transactionId: String, amount: BigDecimal): WithdrawalResponse {
        // 멱등성 체크: 이미 처리된 거래인지 확인
        transactionHistoryRepository.findByTransactionId(transactionId)?.let {
            return WithdrawalResponse.from(it)
        }

        // 분산 락 획득 및 출금 실행
        val lockKey = "wallet:$walletId"
        return lockManager.executeWithLock(lockKey) {
            walletTransactionService.executeWithdrawal(walletId, transactionId, amount)
        }
    }

    /**
     * 잔액 조회
     */
    @Transactional(readOnly = true)
    fun getBalance(walletId: Long): BalanceResponse {
        val wallet = walletRepository.findById(walletId)
            .orElseThrow { WalletNotFoundException(walletId) }

        return BalanceResponse.from(wallet)
    }
}
