package com.jeka8833.tntserverwebapi.websocket.packet.packets

import com.jeka8833.tntserverwebapi.websocket.PacketListener
import com.jeka8833.tntserverwebapi.websocket.packet.Packet
import com.jeka8833.tntserverwebapi.websocket.packet.PacketInputStream
import com.jeka8833.tntserverwebapi.websocket.packet.PacketOutputStream
import okhttp3.WebSocket
import java.util.UUID

class ModulesStatusPacket : Packet {
    private var uniqueID: Int = 0

    private val requestedUser: UUID?
    var playerFound = false;
    var currentActives: Long = 0
    var forceBlock: Long = 0
    var forceActive: Long = 0

    constructor() {
        this.requestedUser = null
    }

    constructor(requestedUser: UUID) {
        this.requestedUser = requestedUser
    }

    override fun setUniqueID(uniqueID: Int) {
        this.uniqueID = uniqueID
    }

    override fun getUniqueID(): Int {
        return uniqueID
    }

    override fun write(stream: PacketOutputStream) {
        stream.writeShort(uniqueID)
        stream.writeUUID(requestedUser!!)
    }

    override fun read(stream: PacketInputStream) {
        uniqueID = stream.readShort().toInt()
        playerFound = stream.readBoolean()

        if (playerFound) {
            currentActives = stream.readLong()
            forceBlock = stream.readLong()
            forceActive = stream.readLong()
        }
    }

    override fun clientProcess(socket: WebSocket) {
        PacketListener.callListener(this)
    }
}