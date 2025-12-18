package com.wallet.withdrawal.domain.vo

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 금액 Value Object
 */
@JvmInline
value class Money(val amount: BigDecimal) : Comparable<Money> {

    init {
        require(amount.scale() <= 2) { "Money scale must not exceed 2 decimal places" }
        require(amount >= BigDecimal.ZERO) { "Money amount cannot be negative: $amount" }
    }

    operator fun plus(other: Money): Money =
        Money(amount.add(other.amount).setScale(2, RoundingMode.HALF_UP))

    operator fun minus(other: Money): Money =
        Money(amount.subtract(other.amount).setScale(2, RoundingMode.HALF_UP))

    override fun compareTo(other: Money): Int = amount.compareTo(other.amount)
}
