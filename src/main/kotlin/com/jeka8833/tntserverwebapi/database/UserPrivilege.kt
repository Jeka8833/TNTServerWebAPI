package com.jeka8833.tntserverwebapi.database

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table(name = "TCA_UserPrivileges")
data class UserPrivilege(
    @Id @Column("user") val user: UUID,
    @Column("staticKey") val staticKey: UUID?,
    @Column("roles") val roles: String,
) {
    fun isUserBlocked(): Boolean = roles.contains("AUTH_BLOCKED")
}
