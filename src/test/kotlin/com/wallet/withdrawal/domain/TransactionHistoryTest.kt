package com.wallet.withdrawal.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * TransactionHistory Entity 단위 테스트
 */
class TransactionHistoryTest {

    @Test
    fun `should create transaction history successfully`() {
        // given
        val wallet = Wallet(id = 1L, balance = BigDecimal("1000.00"))
        val transactionId = "TXN-001"
        val withdrawalAmount = BigDecimal("100.00")
        val remainingBalance = BigDecimal("900.00")

        // when
        val history = TransactionHistory(
            wallet = wallet,
            transactionId = transactionId,
            withdrawalAmount = withdrawalAmount,
            remainingBalance = remainingBalance
        )

        // then
        assertNull(history.id)
        assertEquals(wallet, history.wallet)
        assertEquals(transactionId, history.transactionId)
        assertEquals(withdrawalAmount, history.withdrawalAmount)
        assertEquals(remainingBalance, history.remainingBalance)
        assertNotNull(history.createdAt)
    }

    @Test
    fun `should validate data class equality`() {
        // given
        val wallet = Wallet(id = 1L, balance = BigDecimal("1000.00"))
        val history1 = TransactionHistory(
            id = 1L,
            wallet = wallet,
            transactionId = "TXN-001",
            withdrawalAmount = BigDecimal("100.00"),
            remainingBalance = BigDecimal("900.00")
        )
        val history2 = TransactionHistory(
            id = 1L,
            wallet = wallet,
            transactionId = "TXN-001",
            withdrawalAmount = BigDecimal("100.00"),
            remainingBalance = BigDecimal("900.00")
        )

        // when & then
        assertEquals(history1.id, history2.id)
        assertEquals(history1.wallet, history2.wallet)
        assertEquals(history1.transactionId, history2.transactionId)
    }
}
