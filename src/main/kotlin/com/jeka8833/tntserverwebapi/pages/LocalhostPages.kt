package com.jeka8833.tntserverwebapi.pages

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class LocalhostPages {

    @ResponseBody
    @GetMapping("/api/tempToken/login")
    fun register(): Array<String> {
        return SecurityContextHolder.getContext().authentication.authorities
            .map { value -> value.authority }
            .toTypedArray()
    }
}