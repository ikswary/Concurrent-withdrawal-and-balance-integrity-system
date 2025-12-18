package com.wallet.withdrawal.domain

import com.wallet.withdrawal.domain.vo.Money
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Wallet Entity 단위 테스트
 */
class WalletTest {

    @Test
    fun `should create wallet successfully`() {
        // given
        val balance = Money(BigDecimal("1000.00"))

        // when
        val wallet = Wallet(
            balance = balance
        )

        // then
        assertNull(wallet.id)
        assertEquals(balance, wallet.balance)
        assertNotNull(wallet.createdAt)
        assertNotNull(wallet.updatedAt)
    }

    @Test
    fun `should update wallet balance`() {
        // given
        val wallet = Wallet(
            id = 1L,
            balance = Money(BigDecimal("1000.00"))
        )

        // when
        wallet.balance = Money(BigDecimal("500.00"))
        wallet.updatedAt = LocalDateTime.now()

        // then
        assertEquals(Money(BigDecimal("500.00")), wallet.balance)
    }

    @Test
    fun `should validate data class equality`() {
        // given
        val wallet1 = Wallet(id = 1L, balance = Money(BigDecimal("1000.00")))
        val wallet2 = Wallet(id = 1L, balance = Money(BigDecimal("1000.00")))

        // when & then
        assertEquals(wallet1.id, wallet2.id)
        assertEquals(wallet1.balance, wallet2.balance)
    }
}
