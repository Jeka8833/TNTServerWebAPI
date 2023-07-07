package com.jeka8833.tntserverwebapi.websocket.packet.packets

import com.jeka8833.tntserverwebapi.security.token.TokenManager
import com.jeka8833.tntserverwebapi.security.token.TokenType
import com.jeka8833.tntserverwebapi.websocket.WebSocketClient
import com.jeka8833.tntserverwebapi.websocket.packet.Packet
import com.jeka8833.tntserverwebapi.websocket.packet.PacketInputStream
import com.jeka8833.tntserverwebapi.websocket.packet.PacketOutputStream
import okhttp3.WebSocket
import java.time.LocalDateTime
import java.util.*

class TokenGeneratorPacket : Packet {

    private lateinit var user: UUID
    private lateinit var token: UUID
    private var unregister: Boolean = false

    @Suppress("unused")
    constructor()
    constructor(user: UUID, token: UUID) {
        this.user = user
        this.token = token
    }

    override fun setUniqueID(uniqueID: Int) {
        TODO("The operation is not supported")
    }

    override fun write(stream: PacketOutputStream) {
        stream.writeUUID(user)
        stream.writeUUID(token)
    }

    override fun read(stream: PacketInputStream) {
        user = stream.readUUID()
        unregister = stream.readBoolean()
    }

    override fun clientProcess(socket: WebSocket) {
        if (unregister) {
            TokenManager.removeUser(user)
            WebSocketClient.send(TokenGeneratorPacket(user, UUID(0, 0)))
        } else {
            val token = UUID.randomUUID()
            TokenManager.add(user, token, TokenType.USER, LocalDateTime.now().plusMinutes(5))
            WebSocketClient.send(TokenGeneratorPacket(user, token))
        }
    }
}