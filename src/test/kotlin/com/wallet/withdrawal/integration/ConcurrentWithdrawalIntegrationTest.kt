package com.wallet.withdrawal.integration

import com.wallet.withdrawal.domain.Wallet
import com.wallet.withdrawal.domain.vo.Money
import com.wallet.withdrawal.repository.TransactionHistoryRepository
import com.wallet.withdrawal.repository.WalletRepository
import com.wallet.withdrawal.service.WalletService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Concurrent Withdrawal Integration Test
 * 100개 스레드 동시 출금 테스트
 *
 * 환경:
 * - Database: PostgreSQL (wallet_db_test)
 * - Lock: Redis (Redisson)
 * - Profile: integration (고정)
 */
@SpringBootTest
@ActiveProfiles("integration")
class ConcurrentWithdrawalIntegrationTest(
    @Autowired private val walletRepository: WalletRepository,
    @Autowired private val transactionHistoryRepository: TransactionHistoryRepository,
    @Autowired private val walletService: WalletService
) {
    private var testWalletId: Long = 0

    @BeforeEach
    fun setUp() {
        transactionHistoryRepository.deleteAll()
        walletRepository.deleteAll()

        testWalletId = walletRepository.save(
            Wallet(balance = Money(BigDecimal("10000.00")))
        ).id!!
    }

    @Test
    fun `should handle 100 concurrent withdrawal requests correctly`() {
        // given
        val threadCount = 100
        val withdrawalAmount = BigDecimal("100.00")
        val expectedFinalBalance = BigDecimal("0.00")

        // when - 100 threads withdraw simultaneously
        val (successCount, failureCount) = executeConcurrentWithdrawals(
            threadCount = threadCount,
            walletId = testWalletId,
            amount = withdrawalAmount,
            transactionIdPrefix = "tx-concurrent"
        )

        // then
        assertEquals(threadCount, successCount + failureCount, "Total count should match thread count")

        val wallet = walletRepository.findById(testWalletId).orElseThrow()
        assertEquals(Money(expectedFinalBalance), wallet.balance, "Final balance should be correct")

        val historyCount = transactionHistoryRepository.count()
        assertEquals(threadCount.toLong(), historyCount, "All transactions should be recorded")

        printTestResult("Concurrent Withdrawal Test", successCount, failureCount, wallet.balance.amount, historyCount)
    }

    @Test
    fun `should handle concurrent withdrawals with partial success when balance is insufficient`() {
        // given
        val initialBalance = BigDecimal("5000.00")
        val walletId = walletRepository.save(Wallet(balance = Money(initialBalance))).id!!

        val threadCount = 100
        val withdrawalAmount = BigDecimal("100.00")
        val expectedSuccessCount = 50

        // when - 100 threads try to withdraw, but only 50 should succeed
        val (successCount, failureCount) = executeConcurrentWithdrawals(
            threadCount = threadCount,
            walletId = walletId,
            amount = withdrawalAmount,
            transactionIdPrefix = "tx-partial"
        )

        // then
        assertEquals(threadCount, successCount + failureCount)
        assertEquals(expectedSuccessCount, successCount, "Exactly 50 withdrawals should succeed")
        assertEquals(threadCount - expectedSuccessCount, failureCount, "Exactly 50 withdrawals should fail")

        val finalWallet = walletRepository.findById(walletId).orElseThrow()
        assertEquals(Money(BigDecimal("0.00")), finalWallet.balance, "Final balance should be 0")

        printTestResult("Partial Success Test", successCount, failureCount, finalWallet.balance.amount)
    }

    @Test
    fun `should maintain idempotency with duplicate transaction IDs in concurrent requests`() {
        // given
        val threadCount = 100
        val withdrawalAmount = BigDecimal("100.00")
        val sameTransactionId = "tx-idempotent-same"

        // when - 100 threads try to withdraw with the SAME transaction ID
        val (successCount, failureCount) = executeConcurrentWithdrawals(
            threadCount = threadCount,
            walletId = testWalletId,
            amount = withdrawalAmount,
            fixedTransactionId = sameTransactionId
        )

        // then - Verify balance deducted only once (critical for data integrity)
        val wallet = walletRepository.findById(testWalletId).orElseThrow()
        val expectedBalance = BigDecimal("10000.00") - withdrawalAmount
        assertEquals(Money(expectedBalance), wallet.balance, "Balance should be deducted only once")

        // Verify only one transaction history record (critical for data integrity)
        val historyCount = transactionHistoryRepository.count()
        assertEquals(1L, historyCount, "Only one transaction should be recorded")

        assertTrue(successCount >= 1, "At least one request should succeed")

        printTestResult("Idempotency Test", successCount, failureCount, wallet.balance.amount, historyCount)
    }

    private fun executeConcurrentWithdrawals(
        threadCount: Int,
        walletId: Long,
        amount: BigDecimal,
        transactionIdPrefix: String? = null,
        fixedTransactionId: String? = null
    ): Pair<Int, Int> {
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)
        val executor = Executors.newFixedThreadPool(threadCount)

        try {
            val latch = CountDownLatch(threadCount)

            repeat(threadCount) { i ->
                executor.submit {
                    runCatching {
                        val transactionId = fixedTransactionId ?: "$transactionIdPrefix-${i + 1}"
                        walletService.withdraw(walletId, transactionId, amount)
                        successCount.incrementAndGet()
                    }.onFailure {
                        failureCount.incrementAndGet()
                    }
                    latch.countDown()
                }
            }

            assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete within 30 seconds")
        } finally {
            executor.shutdown()
        }

        return successCount.get() to failureCount.get()
    }

    private fun printTestResult(
        testName: String,
        successCount: Int,
        failureCount: Int,
        finalBalance: BigDecimal,
        historyCount: Long? = null
    ) {
        println("""
            === $testName Result ===
            Success: $successCount
            Failure: $failureCount
            Final Balance: $finalBalance
            ${historyCount?.let { "Transaction History Count: $it" } ?: ""}
        """.trimIndent())
    }
}
