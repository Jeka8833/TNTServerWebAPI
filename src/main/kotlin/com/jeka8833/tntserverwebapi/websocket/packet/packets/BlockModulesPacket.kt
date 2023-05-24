package com.jeka8833.tntserverwebapi.websocket.packet.packets

import com.jeka8833.tntserverwebapi.websocket.packet.Packet
import com.jeka8833.tntserverwebapi.websocket.packet.PacketInputStream
import com.jeka8833.tntserverwebapi.websocket.packet.PacketOutputStream
import okhttp3.WebSocket
import java.util.*

class BlockModulesPacket(private val requestedUser: UUID? = null, private val forceBlock: Long = 0,
                         private val forceActive: Long = 0) : Packet {

    override fun setUniqueID(uniqueID: Int) {
        TODO("The operation is not supported")
    }

    override fun write(stream: PacketOutputStream) {
        stream.writeUUID(requestedUser!!)
        stream.writeLong(forceBlock)
        stream.writeLong(forceActive)
    }

    override fun read(stream: PacketInputStream) {
        TODO("The operation is not supported")
    }

    override fun clientProcess(socket: WebSocket) {
        TODO("The operation is not supported")
    }
}