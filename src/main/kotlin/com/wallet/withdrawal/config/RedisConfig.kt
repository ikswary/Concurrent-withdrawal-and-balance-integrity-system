package com.wallet.withdrawal.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Redis Configuration
 * Redisson 클라이언트 설정
 */
@Configuration
@Profile("!test")
class RedisConfig(
    @Value("\${spring.data.redis.host:localhost}")
    private val redisHost: String,

    @Value("\${spring.data.redis.port:6379}")
    private val redisPort: Int
) {

    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config()
        config.useSingleServer()
            .setAddress("redis://$redisHost:$redisPort")
            .setConnectionMinimumIdleSize(5)
            .setConnectionPoolSize(20)
            .setConnectTimeout(10000)
            .setTimeout(10000)
            .setRetryAttempts(3)
            .setRetryInterval(1500)

        return Redisson.create(config)
    }
}
