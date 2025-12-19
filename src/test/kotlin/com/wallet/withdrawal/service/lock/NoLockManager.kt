package com.wallet.withdrawal.service.lock

/**
 * No Lock Manager (Test Only)
 * 동시성 제어 없이 실행하는 락 매니저 (비교 테스트용)
 */
class NoLockManager : LockManager {
    override fun <T> executeWithLock(lockKey: String, action: () -> T): T {
        // No locking - just execute the action directly
        return action()
    }
}
