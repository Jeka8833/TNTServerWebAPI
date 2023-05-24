package com.jeka8833.tntserverwebapi.websocket.packet

import com.jeka8833.tntserverwebapi.websocket.WebSocketClient.Companion.registeredPackets
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.IOException
import java.util.*

class PacketInputStream(buffer: ByteArray?) : DataInputStream(ByteArrayInputStream(buffer)) {
    val packet: Packet = registeredPackets[readByte()]!!.getDeclaredConstructor().newInstance()!!

    @Throws(IOException::class)
    fun readUUID(): UUID {
        return UUID(readLong(), readLong())
    }
}
