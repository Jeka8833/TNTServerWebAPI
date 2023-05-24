package com.jeka8833.tntserverwebapi.pages

import com.jeka8833.tntserverwebapi.Util
import com.jeka8833.tntserverwebapi.git.GitFileController
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class CurrentUserPages {

    @ResponseBody
    @GetMapping("/api/roles")
    fun roles(): Array<String> {
        return SecurityContextHolder.getContext().authentication.authorities
            .map { value -> value.authority }
            .toTypedArray()
    }

    @ResponseBody
    @PostMapping("/api/cape")
    fun updateCape(@RequestBody skin: PlayerSkin): String {
        val userUUID: UUID = Util.parsePlayerUUID(SecurityContextHolder.getContext().authentication.name)
            ?: return "{\"errorCode\":1, \"errorDescription\":\"Unknown user\"}"

        GitFileController.addTask {
            if (Util.writeImageToFile(
                    GitFileController.getProjectFolder().resolve("capes/$userUUID.png"), skin.cape
                )
            )
                Util.JSON.writeValue(
                    GitFileController.getProjectFolder().resolve("capeData/$userUUID.json").toFile(),
                    PlayerSkinDataFile(if (skin.useTNTCape) 2 else 1)
                )
        }

        return "{\"errorCode\":0}"
    }

    data class PlayerSkin(val useTNTCape: Boolean, val cape: String)
    data class PlayerSkinDataFile(val capePriority: Int)
}