package com.wallet.withdrawal.repository

import com.wallet.withdrawal.domain.TransactionHistory
import com.wallet.withdrawal.domain.Wallet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * TransactionHistory Repository
 */
@Repository
interface TransactionHistoryRepository : JpaRepository<TransactionHistory, Long> {
    /**
     * 거래 ID로 거래 내역 조회 (멱등성 체크용)
     */
    fun findByTransactionId(transactionId: String): TransactionHistory?

    /**
     * 월렛으로 거래 내역 목록 조회
     */
    fun findByWalletOrderByCreatedAtDesc(wallet: Wallet): List<TransactionHistory>

    /**
     * 월렛 ID로 거래 내역 목록 조회
     */
    fun findByWallet_IdOrderByCreatedAtDesc(walletId: Long): List<TransactionHistory>
}
