package com.wallet.withdrawal.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

/**
 * Withdrawal Request DTO
 */
data class WithdrawalRequest(
    @field:NotBlank(message = "Transaction ID is required")
    val transactionId: String,

    @field:DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    val amount: BigDecimal
)
