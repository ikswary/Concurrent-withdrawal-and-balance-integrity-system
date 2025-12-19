package com.wallet.withdrawal.integration

import com.wallet.withdrawal.domain.Wallet
import com.wallet.withdrawal.domain.vo.Money
import com.wallet.withdrawal.repository.TransactionHistoryRepository
import com.wallet.withdrawal.repository.WalletRepository
import com.wallet.withdrawal.service.WalletService
import com.wallet.withdrawal.service.WalletTransactionService
import com.wallet.withdrawal.service.lock.LocalLockManager
import com.wallet.withdrawal.service.lock.NoLockManager
import org.junit.jupiter.api.Assertions.*
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
 * Concurrency Control Comparison Test
 * 동시성 제어 적용 전후 비교 테스트
 *
 * Purpose: 락이 없을 때와 있을 때의 차이를 명확히 보여줌
 */
@SpringBootTest
@ActiveProfiles("integration")
class ConcurrencyControlComparisonTest(
    @Autowired private val walletRepository: WalletRepository,
    @Autowired private val transactionHistoryRepository: TransactionHistoryRepository,
    @Autowired private val walletTransactionService: WalletTransactionService
) {

    @BeforeEach
    fun setUp() {
        transactionHistoryRepository.deleteAll()
        walletRepository.deleteAll()
    }

    @Test
    fun `should show data inconsistency WITHOUT lock control`() {
        // given
        val initialBalance = BigDecimal("10000.00")
        val wallet = walletRepository.save(Wallet(balance = Money(initialBalance)))
        val walletId = wallet.id!!

        val threadCount = 100
        val withdrawalAmount = BigDecimal("100.00")
        val expectedFinalBalance = BigDecimal("0.00")

        // Create service WITHOUT lock
        val noLockService = WalletService(
            walletRepository = walletRepository,
            transactionHistoryRepository = transactionHistoryRepository,
            lockManager = NoLockManager(),
            walletTransactionService = walletTransactionService
        )

        // when - 100 threads withdraw simultaneously WITHOUT LOCK
        val (successCount, failureCount) = executeConcurrentWithdrawals(
            threadCount = threadCount,
            walletId = walletId,
            amount = withdrawalAmount,
            service = noLockService
        )

        // then - Check for data inconsistency
        val finalWallet = walletRepository.findById(walletId).orElseThrow()
        val actualBalance = finalWallet.balance.amount
        val historyCount = transactionHistoryRepository.count()

        println("""
            === WITHOUT LOCK CONTROL (Expected to FAIL) ===
            Initial Balance: $initialBalance
            Thread Count: $threadCount
            Withdrawal Amount per Thread: $withdrawalAmount

            Expected Final Balance: $expectedFinalBalance
            Actual Final Balance: $actualBalance

            Expected Success Count: $threadCount
            Actual Success Count: $successCount
            Actual Failure Count: $failureCount

            Expected History Count: $threadCount
            Actual History Count: $historyCount

            ⚠️ Data Inconsistency Detected: ${actualBalance != expectedFinalBalance}
            ⚠️ Lost Updates: ${historyCount < threadCount}
        """.trimIndent())

        // Verify that WITHOUT lock leads to data inconsistency
        assertTrue(
            actualBalance != expectedFinalBalance || historyCount < threadCount,
            "WITHOUT lock should lead to data inconsistency (race condition)"
        )
    }

    @Test
    fun `should maintain data consistency WITH lock control`() {
        // given
        val initialBalance = BigDecimal("10000.00")
        val wallet = walletRepository.save(Wallet(balance = Money(initialBalance)))
        val walletId = wallet.id!!

        val threadCount = 100
        val withdrawalAmount = BigDecimal("100.00")
        val expectedFinalBalance = BigDecimal("0.00")

        // Create service WITH lock
        val lockService = WalletService(
            walletRepository = walletRepository,
            transactionHistoryRepository = transactionHistoryRepository,
            lockManager = LocalLockManager(),
            walletTransactionService = walletTransactionService
        )

        // when - 100 threads withdraw simultaneously WITH LOCK
        val (successCount, failureCount) = executeConcurrentWithdrawals(
            threadCount = threadCount,
            walletId = walletId,
            amount = withdrawalAmount,
            service = lockService
        )

        // then - Check for data consistency
        val finalWallet = walletRepository.findById(walletId).orElseThrow()
        val actualBalance = finalWallet.balance.amount
        val historyCount = transactionHistoryRepository.count()

        println("""
            === WITH LOCK CONTROL (Expected to PASS) ===
            Initial Balance: $initialBalance
            Thread Count: $threadCount
            Withdrawal Amount per Thread: $withdrawalAmount

            Expected Final Balance: $expectedFinalBalance
            Actual Final Balance: $actualBalance

            Total Operations: ${successCount + failureCount}
            Success Count: $successCount
            Failure Count: $failureCount

            Expected History Count: $threadCount
            Actual History Count: $historyCount

            ✅ Data Consistency: ${actualBalance == expectedFinalBalance}
            ✅ All Transactions Recorded: ${historyCount == threadCount.toLong()}
        """.trimIndent())

        // Verify that WITH lock maintains data consistency
        assertEquals(Money(expectedFinalBalance), finalWallet.balance, "Final balance should be correct")
        assertEquals(threadCount.toLong(), historyCount, "All transactions should be recorded")
        assertEquals(threadCount, successCount + failureCount, "Total count should match")
    }

    @Test
    fun `should demonstrate race condition with visual comparison`() {
        println("""
            ╔═══════════════════════════════════════════════════════════════╗
            ║     CONCURRENCY CONTROL COMPARISON TEST                       ║
            ╚═══════════════════════════════════════════════════════════════╝

            This test demonstrates the importance of concurrency control by
            comparing the results with and without lock mechanisms.

            Scenario:
            - Initial Balance: 1000.00
            - Thread Count: 50
            - Withdrawal Amount per Thread: 20.00
            - Expected Final Balance: 0.00
        """.trimIndent())

        // Setup
        val initialBalance = BigDecimal("1000.00")
        val threadCount = 50
        val withdrawalAmount = BigDecimal("20.00")
        val expectedBalance = BigDecimal("0.00")

        // Test 1: Without Lock
        transactionHistoryRepository.deleteAll()
        walletRepository.deleteAll()
        val wallet1 = walletRepository.save(Wallet(balance = Money(initialBalance)))
        val noLockService = WalletService(
            walletRepository,
            transactionHistoryRepository,
            NoLockManager(),
            walletTransactionService
        )

        val (noLockSuccess, noLockFailure) = executeConcurrentWithdrawals(
            threadCount, wallet1.id!!, withdrawalAmount, noLockService
        )

        val noLockFinalWallet = walletRepository.findById(wallet1.id!!).orElseThrow()
        val noLockHistoryCount = transactionHistoryRepository.count()

        // Test 2: With Lock
        transactionHistoryRepository.deleteAll()
        walletRepository.deleteAll()
        val wallet2 = walletRepository.save(Wallet(balance = Money(initialBalance)))
        val lockService = WalletService(
            walletRepository,
            transactionHistoryRepository,
            LocalLockManager(),
            walletTransactionService
        )

        val (lockSuccess, lockFailure) = executeConcurrentWithdrawals(
            threadCount, wallet2.id!!, withdrawalAmount, lockService
        )

        val lockFinalWallet = walletRepository.findById(wallet2.id!!).orElseThrow()
        val lockHistoryCount = transactionHistoryRepository.count()

        // Print Comparison
        println("""

            ┌─────────────────────────────────────────────────────────────┐
            │                     TEST RESULTS                            │
            ├─────────────────────────────────────────────────────────────┤
            │                    │  WITHOUT LOCK  │   WITH LOCK           │
            ├─────────────────────────────────────────────────────────────┤
            │ Final Balance      │  ${String.format("%12s", noLockFinalWallet.balance.amount)}  │  ${String.format("%12s", lockFinalWallet.balance.amount)}   │
            │ Expected Balance   │  ${String.format("%12s", expectedBalance)}  │  ${String.format("%12s", expectedBalance)}   │
            │ Success Count      │  ${String.format("%12d", noLockSuccess)}  │  ${String.format("%12d", lockSuccess)}   │
            │ Failure Count      │  ${String.format("%12d", noLockFailure)}  │  ${String.format("%12d", lockFailure)}   │
            │ History Records    │  ${String.format("%12d", noLockHistoryCount)}  │  ${String.format("%12d", lockHistoryCount)}   │
            │ Data Consistent?   │  ${String.format("%12s", if (noLockFinalWallet.balance.amount == expectedBalance) "✅ YES" else "❌ NO")}  │  ${String.format("%12s", if (lockFinalWallet.balance.amount == expectedBalance) "✅ YES" else "❌ NO")}   │
            └─────────────────────────────────────────────────────────────┘

            Conclusion:
            ${if (noLockFinalWallet.balance.amount != expectedBalance) "❌ WITHOUT lock: Race condition detected (Lost updates)" else "⚠️ WITHOUT lock: Happened to work this time (not guaranteed)"}
            ${if (lockFinalWallet.balance.amount == expectedBalance) "✅ WITH lock: Data integrity maintained" else "❌ WITH lock: Unexpected failure"}
        """.trimIndent())

        // Assertions
        assertEquals(Money(expectedBalance), lockFinalWallet.balance, "WITH lock should maintain correct balance")
        assertEquals(threadCount.toLong(), lockHistoryCount, "WITH lock should record all transactions")
    }

    private fun executeConcurrentWithdrawals(
        threadCount: Int,
        walletId: Long,
        amount: BigDecimal,
        service: WalletService
    ): Pair<Int, Int> {
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)
        val executor = Executors.newFixedThreadPool(threadCount)

        try {
            val latch = CountDownLatch(threadCount)

            repeat(threadCount) { i ->
                executor.submit {
                    runCatching {
                        service.withdraw(walletId, "tx-comparison-$i", amount)
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
}
