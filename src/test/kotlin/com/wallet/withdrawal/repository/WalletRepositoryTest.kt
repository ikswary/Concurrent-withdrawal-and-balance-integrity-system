package com.wallet.withdrawal.repository

import com.wallet.withdrawal.domain.Wallet
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

/**
 * WalletRepository 단위 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
class WalletRepositoryTest {

    @Autowired
    private lateinit var walletRepository: WalletRepository

    @Test
    fun `should save and find wallet`() {
        // given
        val wallet = Wallet(
            balance = BigDecimal("1000.00")
        )

        // when
        val savedWallet = walletRepository.save(wallet)
        val foundWallet = walletRepository.findById(savedWallet.id!!)

        // then
        assertTrue(foundWallet.isPresent)
        assertEquals(savedWallet.id, foundWallet.get().id)
        assertEquals(BigDecimal("1000.00"), foundWallet.get().balance)
    }

    @Test
    fun `should update wallet balance`() {
        // given
        val wallet = Wallet(
            balance = BigDecimal("1000.00")
        )
        val savedWallet = walletRepository.save(wallet)

        // when
        savedWallet.balance = BigDecimal("500.00")
        walletRepository.save(savedWallet)

        // then
        val updatedWallet = walletRepository.findById(savedWallet.id!!).get()
        assertEquals(BigDecimal("500.00"), updatedWallet.balance)
    }

    @Test
    fun `should delete wallet`() {
        // given
        val wallet = Wallet(
            balance = BigDecimal("1000.00")
        )
        val savedWallet = walletRepository.save(wallet)

        // when
        walletRepository.deleteById(savedWallet.id!!)

        // then
        val foundWallet = walletRepository.findById(savedWallet.id!!)
        assertFalse(foundWallet.isPresent)
    }
}
