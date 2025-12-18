package com.wallet.withdrawal.service.lock

import mu.KotlinLogging
import org.redisson.api.RedissonClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

/**
 * Redis Lock Manager
 * Redisson을 사용한 분산 락 구현
 */
@Component
@Profile("!test")
class RedisLockManager(
    private val redissonClient: RedissonClient
) : LockManager {

    companion object {
        private const val WAIT_TIME = 10L
        private const val LEASE_TIME = 30L
    }

    override fun <T> executeWithLock(lockKey: String, action: () -> T): T {
        val lock = redissonClient.getLock(lockKey)

        logger.debug { "Attempting to acquire lock for key: $lockKey" }

        val acquired = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)

        if (!acquired) {
            logger.error { "Failed to acquire lock for key: $lockKey" }
            throw LockAcquisitionException("Failed to acquire lock: $lockKey")
        }

        return try {
            logger.debug { "Lock acquired for key: $lockKey" }
            action()
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
                logger.debug { "Lock released for key: $lockKey" }
            }
        }
    }
}
