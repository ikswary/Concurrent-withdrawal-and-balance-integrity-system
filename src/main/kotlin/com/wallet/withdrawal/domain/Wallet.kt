package com.wallet.withdrawal.domain

import com.wallet.withdrawal.domain.vo.Money
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Wallet Entity (월렛)
 * bigint 기반 식별자 사용
 */
@Entity
@Table(name = "wallets")
data class Wallet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, precision = 19, scale = 2)
    var balance: Money,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
