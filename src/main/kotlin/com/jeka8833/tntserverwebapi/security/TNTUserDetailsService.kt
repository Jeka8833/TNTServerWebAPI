package com.jeka8833.tntserverwebapi.security

import com.jeka8833.tntserverwebapi.Util
import com.jeka8833.tntserverwebapi.database.UserPrivilege
import com.jeka8833.tntserverwebapi.database.UserPrivilegeRepository
import com.jeka8833.tntserverwebapi.security.token.TokenManager
import com.jeka8833.tntserverwebapi.security.token.TokenType
import com.jeka8833.tntserverwebapi.security.token.UserToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TNTUserDetailsService : UserDetailsService {

    @Autowired
    private lateinit var userPrivilegeRepository: UserPrivilegeRepository

    override fun loadUserByUsername(username: String?): UserDetails {
        val userUUID = Util.parseUUID(username) ?: throw UsernameNotFoundException("Username is invalid")

        val databaseResponse = userPrivilegeRepository.findById(userUUID)
        val dbUser: UserPrivilege? = if (databaseResponse.isEmpty) null else databaseResponse.get()

        val userToken: UserToken =
            if (dbUser?.staticKey != null)
                UserToken(dbUser.staticKey, LocalDateTime.now(), TokenType.BOT, LocalDateTime.now().plusMinutes(1))
            else
                TokenManager.get(userUUID) ?: throw UsernameNotFoundException("Token not found")

        val userBuilder = User
            .withUsername(userUUID.toString())
            .password("{noop}${userToken.token}")

        if (dbUser == null) return userBuilder.build()

        val userDetails: UserDetails = userBuilder
            .disabled(dbUser.isUserBlocked())
            .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList(dbUser.roles))
            .build()
        userToken.userInformation = userDetails as User
        return userDetails
    }
}