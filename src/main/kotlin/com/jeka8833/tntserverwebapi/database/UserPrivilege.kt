package com.jeka8833.tntserverwebapi.database

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.security.core.GrantedAuthority
import java.util.UUID

@Table(name = "TCA_UserPrivileges")
data class UserPrivilege(@Id @Column("user") val user: UUID, @Column("roles") val roles: String) {

    fun isUserBlocked(): Boolean = roles.contains("AUTH_BLOCKED")
}
