package com.jeka8833.tntserverwebapi.security

import com.jeka8833.tntserverwebapi.Util
import com.jeka8833.tntserverwebapi.database.UserPrivilegeRepository
import com.jeka8833.tntserverwebapi.security.token.TokenManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class TNTUserDetailsService : UserDetailsService {

    @Autowired
    private lateinit var userPrivilegeRepository: UserPrivilegeRepository

    override fun loadUserByUsername(username: String?): UserDetails {
        val userUUID = Util.parseUUID(username) ?: throw UsernameNotFoundException("Username is invalid")
        val tokenUUID = TokenManager.get(userUUID)?.token ?: throw UsernameNotFoundException("Token not found")

        val databaseResponse = userPrivilegeRepository.findById(userUUID)

        val userBuilder = User
            .withUsername(userUUID.toString())
            .password("{noop}$tokenUUID")
        if (databaseResponse.isEmpty) return userBuilder.build()

        val dbUser = databaseResponse.get()
        return userBuilder
            .disabled(dbUser.isUserBlocked())
            .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList(dbUser.roles))
            .build()
    }
}