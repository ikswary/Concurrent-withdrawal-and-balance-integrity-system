package com.wallet.withdrawal.repository

import com.wallet.withdrawal.domain.TransactionHistory
import com.wallet.withdrawal.domain.Wallet
import com.wallet.withdrawal.domain.vo.Money
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

/**
 * TransactionHistoryRepository 단위 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
class TransactionHistoryRepositoryTest {

    @Autowired
    private lateinit var transactionHistoryRepository: TransactionHistoryRepository

    @Autowired
    private lateinit var walletRepository: WalletRepository

    @Test
    fun `should save and find transaction history`() {
        // given
        val wallet = walletRepository.save(Wallet(balance = Money(BigDecimal("1000.00"))))
        val history = TransactionHistory(
            wallet = wallet,
            transactionId = "TXN-001",
            withdrawalAmount = Money(BigDecimal("100.00")),
            remainingBalance = Money(BigDecimal("900.00"))
        )

        // when
        val savedHistory = transactionHistoryRepository.save(history)
        val foundHistory = transactionHistoryRepository.findById(savedHistory.id!!)

        // then
        assertTrue(foundHistory.isPresent)
        assertEquals(savedHistory.id, foundHistory.get().id)
        assertEquals("TXN-001", foundHistory.get().transactionId)
    }

    @Test
    fun `should find transaction history by transaction id`() {
        // given
        val wallet = walletRepository.save(Wallet(balance = Money(BigDecimal("1000.00"))))
        val history = TransactionHistory(
            wallet = wallet,
            transactionId = "TXN-001",
            withdrawalAmount = Money(BigDecimal("100.00")),
            remainingBalance = Money(BigDecimal("900.00"))
        )
        transactionHistoryRepository.save(history)

        // when
        val foundHistory = transactionHistoryRepository.findByTransactionId("TXN-001")

        // then
        assertNotNull(foundHistory)
        assertEquals("TXN-001", foundHistory?.transactionId)
        assertEquals(wallet.id, foundHistory?.wallet?.id)
    }

    @Test
    fun `should return null for non-existent transaction id`() {
        // when
        val foundHistory = transactionHistoryRepository.findByTransactionId("NON-EXISTENT")

        // then
        assertNull(foundHistory)
    }

    @Test
    fun `should access wallet id property`() {
        // given
        val wallet = walletRepository.save(Wallet(balance = Money(BigDecimal("1000.00"))))
        val history = TransactionHistory(
            wallet = wallet,
            transactionId = "TXN-001",
            withdrawalAmount = Money(BigDecimal("100.00")),
            remainingBalance = Money(BigDecimal("900.00"))
        )
        val savedHistory = transactionHistoryRepository.save(history)

        // when
        val walletId = savedHistory.walletId

        // then
        assertEquals(wallet.id, walletId)
    }
}
