package com.wallet.withdrawal.service.lock

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

/**
 * Local Lock Manager (Test Only)
 * 테스트 환경에서만 사용되는 ReentrantLock 기반 동시성 제어
 */
@Component
@Profile("test")
class LocalLockManager : LockManager {

    private val locks = ConcurrentHashMap<String, ReentrantLock>()

    override fun <T> executeWithLock(lockKey: String, action: () -> T): T {
        val lock = locks.computeIfAbsent(lockKey) { ReentrantLock() }
        lock.lock()
        return try {
            action()
        } finally {
            lock.unlock()
        }
    }
}
