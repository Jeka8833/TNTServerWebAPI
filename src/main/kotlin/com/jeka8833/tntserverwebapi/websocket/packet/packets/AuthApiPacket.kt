package com.jeka8833.tntserverwebapi.websocket.packet.packets

import com.jeka8833.tntserverwebapi.websocket.packet.Packet
import com.jeka8833.tntserverwebapi.websocket.packet.PacketInputStream
import com.jeka8833.tntserverwebapi.websocket.packet.PacketOutputStream
import okhttp3.WebSocket
import java.util.*

class AuthApiPacket : Packet {
    private val user: UUID?
    private val password: UUID?

    constructor() {
        this.user = null
        this.password = null
    }

    constructor(user: UUID, password: UUID) {
        this.user = user
        this.password = password
    }

    override fun setUniqueID(uniqueID: Int) {
        TODO("The operation is not supported")
    }

    override fun write(stream: PacketOutputStream) {
        stream.writeUUID(user!!)
        stream.writeUUID(password!!)
    }

    override fun read(stream: PacketInputStream) {
    }

    override fun clientProcess(socket: WebSocket) {
    }
}