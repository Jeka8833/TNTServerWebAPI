package com.jeka8833.tntserverwebapi.security.token

import org.springframework.security.core.session.SessionInformation
import org.springframework.security.core.userdetails.User
import java.time.LocalDateTime
import java.util.*

data class UserToken(
    val token: UUID,
    val timeCreate: LocalDateTime,
    val type: TokenType,
    var timeExpiration: LocalDateTime = LocalDateTime.MAX,
    var userInformation: User? = null,
) {

    fun invalidate() {
        timeExpiration = LocalDateTime.MIN

        if (userInformation == null) return

        val sessions: List<SessionInformation> =
            TokenManager.sessionRegistry.getAllSessions(userInformation, false)

        for (session: SessionInformation in sessions) session.expireNow()
    }

    fun isExpire(): Boolean = timeExpiration.isBefore(LocalDateTime.now())

}