package com.wallet.withdrawal.domain.exception

/**
 * 월렛을 찾을 수 없음 예외
 */
class WalletNotFoundException(
    val walletId: Long
) : RuntimeException("Wallet not found: walletId=$walletId")
