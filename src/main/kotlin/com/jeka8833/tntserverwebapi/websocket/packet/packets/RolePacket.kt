package com.jeka8833.tntserverwebapi.websocket.packet.packets

import com.jeka8833.tntserverwebapi.database.UserPrivilege
import com.jeka8833.tntserverwebapi.websocket.WebSocketClient
import com.jeka8833.tntserverwebapi.websocket.packet.Packet
import com.jeka8833.tntserverwebapi.websocket.packet.PacketInputStream
import com.jeka8833.tntserverwebapi.websocket.packet.PacketOutputStream
import okhttp3.WebSocket
import java.util.*

class RolePacket : Packet {
    private var uniqueID: Int = 0
    private var playerUUID: UUID? = null
    private var roles: String? = null

    @Suppress("unused")
    constructor()
    constructor(uniqueID: Int, playerUUID: UUID, roles: String) {
        this.uniqueID = uniqueID
        this.playerUUID = playerUUID
        this.roles = roles
    }

    override fun setUniqueID(uniqueID: Int) {
        this.uniqueID = uniqueID
    }

    override fun write(stream: PacketOutputStream) {
        stream.writeShort(uniqueID)

        stream.writeUTF(roles!!)
    }

    override fun read(stream: PacketInputStream) {
        uniqueID = stream.readShort().toInt()

        playerUUID = stream.readUUID()
    }

    override fun clientProcess(socket: WebSocket) {
        val databaseResponse = WebSocketClient.privilegeRepository.findById(playerUUID!!)
        val dbUser: UserPrivilege? = if (databaseResponse.isEmpty) null else databaseResponse.get()

        WebSocketClient.send(RolePacket(uniqueID, playerUUID!!, dbUser?.roles ?: "UNKNOWN"))
    }
}