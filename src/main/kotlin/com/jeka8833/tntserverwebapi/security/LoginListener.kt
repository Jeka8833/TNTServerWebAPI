package com.jeka8833.tntserverwebapi.security

import com.jeka8833.tntserverwebapi.Util
import com.jeka8833.tntserverwebapi.security.token.TokenManager
import com.jeka8833.tntserverwebapi.security.token.TokenType
import com.jeka8833.tntserverwebapi.websocket.WebSocketClient
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class LoginListener : ApplicationListener<AuthenticationSuccessEvent> {

    @Bean
    fun applicationListener(): ApplicationListener<*>? {
        return LoginListener()
    }

    override fun onApplicationEvent(event: AuthenticationSuccessEvent) {
        val userDetails = event.authentication.principal as UserDetails
        val userUUID = Util.parseUUID(userDetails.username) ?: return
        val userToken = TokenManager.get(userUUID) ?: return

        when (userToken.type) {
            TokenType.USER -> {
                userToken.timeExpiration = userToken.timeCreate.plusDays(7)
            }

            TokenType.BOT -> {
                userToken.timeExpiration = userToken.timeCreate.plusYears(1)
            }

            TokenType.TNTAPI -> {
                TokenManager.removeUser(userUUID)
                WebSocketClient.setStateConnected()
            }
        }
    }
}