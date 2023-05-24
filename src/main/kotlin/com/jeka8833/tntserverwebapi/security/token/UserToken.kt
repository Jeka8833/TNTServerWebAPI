package com.jeka8833.tntserverwebapi.security.token

import java.time.LocalDateTime
import java.util.*

data class UserToken(
    val token: UUID,
    val timeCreate: LocalDateTime,
    val type: TokenType,
    var timeExpiration: LocalDateTime = LocalDateTime.MAX,
) {

    fun invalidate() {
        timeExpiration = LocalDateTime.MIN
    }

    fun isExpire(): Boolean = timeExpiration.isBefore(LocalDateTime.now())

}