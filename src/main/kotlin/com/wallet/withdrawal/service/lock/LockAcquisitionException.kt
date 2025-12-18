package com.wallet.withdrawal.service.lock

import com.wallet.withdrawal.domain.exception.CommonException
import org.springframework.http.HttpStatus

/**
 * Lock Acquisition Exception
 * 락 획득 실패 시 발생하는 예외
 * HTTP 429 Too Many Requests
 */
class LockAcquisitionException(message: String) : CommonException(
    message = message,
    httpStatus = HttpStatus.TOO_MANY_REQUESTS
)
