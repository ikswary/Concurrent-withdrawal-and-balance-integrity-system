package com.wallet.withdrawal.dto

import java.time.LocalDateTime

/**
 * Error Response DTO
 */
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null
)
