package com.jeka8833.tntserverwebapi.websocket.packet

import com.jeka8833.tntserverwebapi.websocket.packet.PacketInputStream
import com.jeka8833.tntserverwebapi.websocket.packet.PacketOutputStream
import okhttp3.WebSocket
import java.io.IOException

interface Packet {
    fun setUniqueID(uniqueID: Int)
    fun getUniqueID(): Int = Int.MAX_VALUE

    @Throws(IOException::class)
    fun write(stream: PacketOutputStream)

    @Throws(IOException::class)
    fun read(stream: PacketInputStream)
    fun clientProcess(socket: WebSocket)
}
