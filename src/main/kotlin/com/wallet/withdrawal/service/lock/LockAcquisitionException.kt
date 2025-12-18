package com.wallet.withdrawal.service.lock

/**
 * Lock Acquisition Exception
 * 락 획득 실패 시 발생하는 예외
 */
class LockAcquisitionException(message: String) : RuntimeException(message)
