package com.wallet.withdrawal.domain

import com.wallet.withdrawal.domain.vo.Money
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * TransactionHistory Entity (거래 내역)
 * 출금 거래 내역을 기록
 */
@Entity
@Table(
    name = "transaction_histories",
    indexes = [
        Index(name = "idx_transaction_id", columnList = "transactionId", unique = true),
        Index(name = "idx_wallet_id", columnList = "wallet_id")
    ]
)
data class TransactionHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false, foreignKey = ForeignKey(name = "fk_transaction_history_wallet"))
    val wallet: Wallet,

    @Column(nullable = false, unique = true, length = 100)
    val transactionId: String,

    @Column(nullable = false, precision = 19, scale = 2)
    val withdrawalAmount: Money,

    @Column(nullable = false, precision = 19, scale = 2)
    val remainingBalance: Money,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Wallet ID를 직접 접근할 수 있도록 제공 (Lazy Loading 회피)
     */
    val walletId: Long
        get() = wallet.id!!
}
