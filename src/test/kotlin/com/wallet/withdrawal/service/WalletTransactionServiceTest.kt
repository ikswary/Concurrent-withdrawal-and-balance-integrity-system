package com.wallet.withdrawal.service

import com.wallet.withdrawal.domain.Wallet
import com.wallet.withdrawal.domain.exception.InsufficientBalanceException
import com.wallet.withdrawal.domain.exception.WalletNotFoundException
import com.wallet.withdrawal.domain.vo.Money
import com.wallet.withdrawal.repository.TransactionHistoryRepository
import com.wallet.withdrawal.repository.WalletRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WalletTransactionServiceTest {

    @Autowired
    private lateinit var walletRepository: WalletRepository

    @Autowired
    private lateinit var transactionHistoryRepository: TransactionHistoryRepository

    @Autowired
    private lateinit var walletTransactionService: WalletTransactionService

    private var testWalletId: Long = 0

    @BeforeEach
    fun setUp() {
        // Create a test wallet
        val wallet = Wallet(
            balance = Money(BigDecimal("1000.00"))
        )
        val savedWallet = walletRepository.save(wallet)
        testWalletId = savedWallet.id!!
    }

    @Test
    fun `should execute withdrawal successfully when wallet exists and has sufficient balance`() {
        // given
        val transactionId = "tx-001"
        val amount = BigDecimal("100.00")

        // when
        val response = walletTransactionService.executeWithdrawal(testWalletId, transactionId, amount)

        // then
        assertEquals(testWalletId, response.walletId)
        assertEquals(transactionId, response.transactionId)
        assertEquals(Money(amount), response.withdrawalAmount)
        assertEquals(Money(BigDecimal("900.00")), response.remainingBalance)

        // Verify wallet balance updated
        val wallet = walletRepository.findById(testWalletId).get()
        assertEquals(Money(BigDecimal("900.00")), wallet.balance)

        // Verify transaction history created
        val history = transactionHistoryRepository.findByTransactionId(transactionId)
        assertNotNull(history)
    }

    @Test
    fun `should throw WalletNotFoundException when wallet does not exist`() {
        // given
        val nonExistentWalletId = 999999L
        val transactionId = "tx-002"
        val amount = BigDecimal("100.00")

        // when & then
        val exception = assertThrows<WalletNotFoundException> {
            walletTransactionService.executeWithdrawal(nonExistentWalletId, transactionId, amount)
        }

        assertEquals(nonExistentWalletId, exception.walletId)
    }

    @Test
    fun `should throw InsufficientBalanceException when wallet has insufficient balance`() {
        // given
        val transactionId = "tx-003"
        val amount = BigDecimal("1500.00") // More than available

        // when & then
        val exception = assertThrows<InsufficientBalanceException> {
            walletTransactionService.executeWithdrawal(testWalletId, transactionId, amount)
        }

        assertEquals(BigDecimal("1000.00"), exception.currentBalance)
        assertEquals(amount, exception.requestedAmount)

        // Verify wallet balance not changed
        val wallet = walletRepository.findById(testWalletId).get()
        assertEquals(Money(BigDecimal("1000.00")), wallet.balance)
    }
}
