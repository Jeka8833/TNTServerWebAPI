package com.jeka8833.tntserverwebapi.websocket.packet.packets

import com.jeka8833.tntserverwebapi.websocket.packet.Packet
import com.jeka8833.tntserverwebapi.websocket.packet.PacketInputStream
import com.jeka8833.tntserverwebapi.websocket.packet.PacketOutputStream
import com.jeka8833.tntserverwebapi.security.token.TokenManager
import com.jeka8833.tntserverwebapi.security.token.TokenType
import okhttp3.WebSocket
import java.time.LocalDateTime
import java.util.*

class AuthApiPacket : Packet {
    override fun setUniqueID(uniqueID: Int) {
        TODO("The operation is not supported")
    }

    override fun write(stream: PacketOutputStream) {
        val token = UUID.randomUUID()

        TokenManager.add(TokenManager.TNT_API_USER, token, TokenType.TNTAPI, LocalDateTime.now().plusMinutes(1))

        stream.writeUUID(TokenManager.TNT_API_USER)
        stream.writeUUID(token)
    }

    override fun read(stream: PacketInputStream) {
    }

    override fun clientProcess(socket: WebSocket) {
    }
}