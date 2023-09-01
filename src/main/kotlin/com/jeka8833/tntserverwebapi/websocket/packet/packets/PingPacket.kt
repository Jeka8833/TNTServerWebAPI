package com.jeka8833.tntserverwebapi.websocket.packet.packets

import com.jeka8833.tntserverwebapi.websocket.packet.Packet
import com.jeka8833.tntserverwebapi.websocket.packet.PacketInputStream
import com.jeka8833.tntserverwebapi.websocket.packet.PacketOutputStream
import okhttp3.WebSocket

class PingPacket(private var value: Long = 0) : Packet {

    override fun setUniqueID(uniqueID: Int) {
        TODO("Not yet implemented")
    }

    override fun write(stream: PacketOutputStream) {
        stream.writeLong(value)
    }

    override fun read(stream: PacketInputStream) {
        value = stream.readLong()
    }

    override fun clientProcess(socket: WebSocket) {
        println("Pong")
    }
}