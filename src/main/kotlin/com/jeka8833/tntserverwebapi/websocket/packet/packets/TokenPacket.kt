package com.jeka8833.tntserverwebapi.websocket.packet.packets

import com.jeka8833.tntserverwebapi.security.token.TokenManager
import com.jeka8833.tntserverwebapi.security.token.TokenType
import com.jeka8833.tntserverwebapi.websocket.packet.Packet
import com.jeka8833.tntserverwebapi.websocket.packet.PacketInputStream
import com.jeka8833.tntserverwebapi.websocket.packet.PacketOutputStream
import okhttp3.WebSocket
import java.time.LocalDateTime
import java.util.*

class TokenPacket : Packet {

    private lateinit var user: UUID
    private lateinit var token: UUID

    override fun setUniqueID(uniqueID: Int) {
        TODO("The operation is not supported")
    }

    override fun write(stream: PacketOutputStream) {
        TODO("The operation is not supported")
    }

    override fun read(stream: PacketInputStream) {
        user = stream.readUUID()
        token = stream.readUUID()
    }

    override fun clientProcess(socket: WebSocket) {
        if (token == UUID(0, 0)) {
            TokenManager.removeUser(user)
        } else {
            TokenManager.add(user, token, TokenType.USER, LocalDateTime.now().plusMinutes(5))
        }
    }
}