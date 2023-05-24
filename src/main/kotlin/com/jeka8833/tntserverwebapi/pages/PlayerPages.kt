package com.jeka8833.tntserverwebapi.pages

import com.jeka8833.tntserverwebapi.Util
import com.jeka8833.tntserverwebapi.websocket.PacketListener
import com.jeka8833.tntserverwebapi.websocket.WebSocketClient
import com.jeka8833.tntserverwebapi.websocket.packet.packets.BlockModulesPacket
import com.jeka8833.tntserverwebapi.websocket.packet.packets.ModulesStatusPacket
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
class PlayerPages {

    @ResponseBody
    @GetMapping("/api/player/{user}/modules")
    fun playerModulesList(@PathVariable user: String?): PlayerModulesInfo {
        val userUUID = Util.parseUUID(user)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found")

        val packet = PacketListener.sendGetResponse(ModulesStatusPacket(userUUID))
        if (packet !is ModulesStatusPacket) throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)

        if (!packet.playerFound) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found")

        return PlayerModulesInfo(userUUID, packet.currentActives, packet.forceActive, packet.forceBlock)
    }

    data class PlayerModulesInfo(val user: UUID, val currentActive: Long, val forceActive: Long, val forceBlock: Long)

    @ResponseBody
    @PutMapping("/api/player/{user}/modules")
    fun playerModulesEdit(@PathVariable user: String?, @RequestBody edit: EditPlayerModules): String {
        val userUUID = Util.parseUUID(user)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found")

        return "{\"success\":" + WebSocketClient.send(
            BlockModulesPacket(userUUID, edit.forceBlock, edit.forceActive)
        ) + "}"
    }

    data class EditPlayerModules(val forceActive: Long, val forceBlock: Long)
}