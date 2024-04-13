package com.jeka8833.tntserverwebapi.database

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserPrivilegeRepository : CrudRepository<UserPrivilege, UUID>