package com.wallet.withdrawal.repository

import com.wallet.withdrawal.domain.Wallet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Wallet Repository
 */
@Repository
interface WalletRepository : JpaRepository<Wallet, Long>
