package com.wallet.withdrawal.service

import com.wallet.withdrawal.domain.Wallet
import com.wallet.withdrawal.domain.exception.InsufficientBalanceException
import com.wallet.withdrawal.domain.exception.WalletNotFoundException
import com.wallet.withdrawal.domain.vo.Money
import com.wallet.withdrawal.repository.TransactionHistoryRepository
import com.wallet.withdrawal.repository.WalletRepository
import org.junit.jupiter.api.Assertions.assertEquals
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
class WalletServiceTest {

    @Autowired
    private lateinit var walletRepository: WalletRepository

    @Autowired
    private lateinit var transactionHistoryRepository: TransactionHistoryRepository

    @Autowired
    private lateinit var walletService: WalletService

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
    fun `should successfully withdraw when transaction is new`() {
        // given
        val transactionId = "tx-001"
        val amount = BigDecimal("100.00")

        // when
        val response = walletService.withdraw(testWalletId, transactionId, amount)

        // then
        assertEquals(transactionId, response.transactionId)
        assertEquals(testWalletId, response.walletId)
        assertEquals(amount, response.withdrawalAmount)
        assertEquals(BigDecimal("900.00"), response.remainingBalance)

        // Verify wallet balance updated
        val wallet = walletRepository.findById(testWalletId).get()
        assertEquals(Money(BigDecimal("900.00")), wallet.balance)

        // Verify transaction history created
        val history = transactionHistoryRepository.findByTransactionId(transactionId)
        assertEquals(transactionId, history?.transactionId)
    }

    @Test
    fun `should return existing transaction result when transaction is duplicate`() {
        // given
        val transactionId = "tx-duplicate"
        val amount = BigDecimal("100.00")

        // First withdrawal
        val firstResponse = walletService.withdraw(testWalletId, transactionId, amount)

        // when - Second withdrawal with same transactionId
        val secondResponse = walletService.withdraw(testWalletId, transactionId, amount)

        // then - Should return same result
        assertEquals(firstResponse.transactionId, secondResponse.transactionId)
        assertEquals(firstResponse.withdrawalAmount, secondResponse.withdrawalAmount)
        assertEquals(firstResponse.remainingBalance, secondResponse.remainingBalance)

        // Verify wallet balance only deducted once
        val wallet = walletRepository.findById(testWalletId).get()
        assertEquals(Money(BigDecimal("900.00")), wallet.balance)
    }

    @Test
    fun `should return balance when wallet exists`() {
        // when
        val response = walletService.getBalance(testWalletId)

        // then
        assertEquals(testWalletId, response.walletId)
        assertEquals(BigDecimal("1000.00"), response.balance)
    }

    @Test
    fun `should throw WalletNotFoundException when wallet does not exist`() {
        // given
        val nonExistentWalletId = 999999L

        // when & then
        assertThrows<WalletNotFoundException> {
            walletService.getBalance(nonExistentWalletId)
        }
    }

    @Test
    fun `should throw InsufficientBalanceException when balance is insufficient`() {
        // given
        val transactionId = "tx-insufficient"
        val amount = BigDecimal("2000.00") // More than available

        // when & then
        assertThrows<InsufficientBalanceException> {
            walletService.withdraw(testWalletId, transactionId, amount)
        }

        // Verify wallet balance not changed
        val wallet = walletRepository.findById(testWalletId).get()
        assertEquals(Money(BigDecimal("1000.00")), wallet.balance)
    }
}
