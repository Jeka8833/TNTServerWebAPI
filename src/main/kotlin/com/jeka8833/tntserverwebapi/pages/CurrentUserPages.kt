package com.jeka8833.tntserverwebapi.pages

import com.jeka8833.tntserverwebapi.Util
import com.jeka8833.tntserverwebapi.git.PlayerCustomization
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
    fun updateCape(@RequestBody cape: PlayerCape): String {
        val userUUID: UUID = Util.parsePlayerUUID(SecurityContextHolder.getContext().authentication.name)
            ?: return "{\"errorCode\":1, \"errorDescription\":\"Unknown user\"}"

        val errorCode = PlayerCustomization.editCape(userUUID, cape.cape, cape.useTNTCape)

        return "{\"errorCode\":$errorCode}"
    }

    @ResponseBody
    @PostMapping("/api/heart")
    fun updateHeart(@RequestBody heart: PlayerHeart): String {
        val userUUID: UUID = Util.parsePlayerUUID(SecurityContextHolder.getContext().authentication.name)
            ?: return "{\"errorCode\":1, \"errorDescription\":\"Unknown user\"}"

        heart.fixAnimation()

        if (!heart.isValid()) return "{\"errorCode\":1, \"errorDescription\":\"Invalid parameters\"}"

        val errorCode = PlayerCustomization.editHeart(userUUID, heart.textAnimation, heart.delayTime)

        return "{\"errorCode\":$errorCode}"
    }

    data class PlayerCape(val useTNTCape: Boolean = true, val cape: String = "")
    data class PlayerHeart(val textAnimation: Array<String> = emptyArray(), val delayTime: Int = Int.MAX_VALUE) {
        fun isValid(): Boolean {
            if (textAnimation.isEmpty() || textAnimation.size > 16) return false

            textAnimation.forEach { text ->
                if (text.length > 32 || (Util.stripColor(text)?.length ?: 0) > 8) return false
            }

            return delayTime > 0
        }

        fun fixAnimation() {
            for (i in textAnimation.indices) {
                textAnimation[i] = Util.translateAlternateColorCodes('&', textAnimation[i]) +
                        Util.COLOR_CHAR + 'r'
            }
        }
    }
}