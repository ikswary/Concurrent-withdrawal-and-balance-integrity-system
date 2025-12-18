package com.wallet.withdrawal.repository

import com.wallet.withdrawal.domain.TransactionHistory
import com.wallet.withdrawal.domain.Wallet
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.math.BigDecimal

/**
 * TransactionHistoryRepository 단위 테스트
 */
@DataJpaTest
class TransactionHistoryRepositoryTest {

    @Autowired
    private lateinit var transactionHistoryRepository: TransactionHistoryRepository

    @Autowired
    private lateinit var walletRepository: WalletRepository

    @Test
    fun `TransactionHistory 저장 및 조회 테스트`() {
        // given
        val wallet = walletRepository.save(Wallet(balance = BigDecimal("1000.00")))
        val history = TransactionHistory(
            wallet = wallet,
            transactionId = "TXN-001",
            withdrawalAmount = BigDecimal("100.00"),
            remainingBalance = BigDecimal("900.00")
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
    fun `transactionId로 조회 테스트`() {
        // given
        val wallet = walletRepository.save(Wallet(balance = BigDecimal("1000.00")))
        val history = TransactionHistory(
            wallet = wallet,
            transactionId = "TXN-001",
            withdrawalAmount = BigDecimal("100.00"),
            remainingBalance = BigDecimal("900.00")
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
    fun `존재하지 않는 transactionId 조회 테스트`() {
        // when
        val foundHistory = transactionHistoryRepository.findByTransactionId("NON-EXISTENT")

        // then
        assertNull(foundHistory)
    }

    @Test
    fun `wallet으로 거래 내역 목록 조회 테스트`() {
        // given
        val wallet1 = walletRepository.save(Wallet(balance = BigDecimal("1000.00")))
        val wallet2 = walletRepository.save(Wallet(balance = BigDecimal("1000.00")))

        val history1 = TransactionHistory(
            wallet = wallet1,
            transactionId = "TXN-001",
            withdrawalAmount = BigDecimal("100.00"),
            remainingBalance = BigDecimal("900.00")
        )
        val history2 = TransactionHistory(
            wallet = wallet1,
            transactionId = "TXN-002",
            withdrawalAmount = BigDecimal("200.00"),
            remainingBalance = BigDecimal("700.00")
        )
        val history3 = TransactionHistory(
            wallet = wallet2,
            transactionId = "TXN-003",
            withdrawalAmount = BigDecimal("50.00"),
            remainingBalance = BigDecimal("950.00")
        )

        transactionHistoryRepository.save(history1)
        Thread.sleep(10) // createdAt 차이를 위해
        transactionHistoryRepository.save(history2)
        transactionHistoryRepository.save(history3)

        // when
        val histories = transactionHistoryRepository.findByWalletOrderByCreatedAtDesc(wallet1)

        // then
        assertEquals(2, histories.size)
        assertEquals("TXN-002", histories[0].transactionId) // 최신순
        assertEquals("TXN-001", histories[1].transactionId)
    }

    @Test
    fun `walletId로 거래 내역 목록 조회 테스트`() {
        // given
        val wallet1 = walletRepository.save(Wallet(balance = BigDecimal("1000.00")))
        val wallet2 = walletRepository.save(Wallet(balance = BigDecimal("1000.00")))

        val history1 = TransactionHistory(
            wallet = wallet1,
            transactionId = "TXN-001",
            withdrawalAmount = BigDecimal("100.00"),
            remainingBalance = BigDecimal("900.00")
        )
        val history2 = TransactionHistory(
            wallet = wallet1,
            transactionId = "TXN-002",
            withdrawalAmount = BigDecimal("200.00"),
            remainingBalance = BigDecimal("700.00")
        )
        val history3 = TransactionHistory(
            wallet = wallet2,
            transactionId = "TXN-003",
            withdrawalAmount = BigDecimal("50.00"),
            remainingBalance = BigDecimal("950.00")
        )

        transactionHistoryRepository.save(history1)
        Thread.sleep(10) // createdAt 차이를 위해
        transactionHistoryRepository.save(history2)
        transactionHistoryRepository.save(history3)

        // when
        val histories = transactionHistoryRepository.findByWallet_IdOrderByCreatedAtDesc(wallet1.id!!)

        // then
        assertEquals(2, histories.size)
        assertEquals("TXN-002", histories[0].transactionId) // 최신순
        assertEquals("TXN-001", histories[1].transactionId)
    }

    @Test
    fun `walletId 속성 접근 테스트`() {
        // given
        val wallet = walletRepository.save(Wallet(balance = BigDecimal("1000.00")))
        val history = TransactionHistory(
            wallet = wallet,
            transactionId = "TXN-001",
            withdrawalAmount = BigDecimal("100.00"),
            remainingBalance = BigDecimal("900.00")
        )
        val savedHistory = transactionHistoryRepository.save(history)

        // when
        val walletId = savedHistory.walletId

        // then
        assertEquals(wallet.id, walletId)
    }
}
