package com.wallet.withdrawal.service.lock

/**
 * Lock Manager Interface
 * 동시성 제어를 위한 락 관리 인터페이스
 */
interface LockManager {
    /**
     * 락을 획득하고 작업을 실행한 후 락을 해제합니다.
     *
     * @param lockKey 락 키 (예: "wallet:123")
     * @param action 락을 획득한 후 실행할 작업
     * @return 작업 실행 결과
     */
    fun <T> executeWithLock(lockKey: String, action: () -> T): T
}
