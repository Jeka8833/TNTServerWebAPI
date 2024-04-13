package com.jeka8833.tntserverwebapi.security.token

import com.jeka8833.tntserverwebapi.Util
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
@EnableScheduling
class TokenManager {

    @Scheduled(fixedRate = 10 * 60 * 1000)  // 10 minutes
    fun deleteOldTokens() = deleteAllExpired()

    companion object {
        val TNT_API_USER: UUID = Util.parseUUID("6bd6e833-a80a-430e-1029-4786368811f9")!!

        private val userSession = ConcurrentHashMap<UUID, UserToken>()
        val sessionRegistry: SessionRegistry = SessionRegistryImpl()

        fun add(user: UUID, token: UUID, tokenType: TokenType, timeExpiration: LocalDateTime) {
            removeUser(user)

            userSession[user] = UserToken(token, LocalDateTime.now(), tokenType, timeExpiration)
        }

        fun get(user: UUID): UserToken? {
            val userToken = userSession[user] ?: return null

            if (userToken.isExpire()) {
                removeUser(user)

                return null
            }
            return userToken
        }

        fun removeUser(user: UUID) {
            userSession.remove(user)?.invalidate()
        }

        private fun deleteAllExpired() {
            userSession.values.removeIf { value ->
                val needDelete: Boolean = value.isExpire()
                if (needDelete) value.invalidate()

                return@removeIf needDelete
            }
        }
    }
}