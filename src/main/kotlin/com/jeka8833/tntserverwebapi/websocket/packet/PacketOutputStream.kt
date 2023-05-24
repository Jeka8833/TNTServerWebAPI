package com.jeka8833.tntserverwebapi.websocket.packet

import com.jeka8833.tntserverwebapi.websocket.WebSocketClient.Companion.registeredPackets
import java.io.*
import java.nio.ByteBuffer
import java.util.*

class PacketOutputStream
/**
 * Creates a new data output stream to write data to the specified
 * underlying output stream. The counter `written` is
 * set to zero.
 *
 * @see FilterOutputStream.out
 */
    : DataOutputStream(ByteArrayOutputStream()) {
    @Throws(IOException::class)
    fun writeUUID(uuid: UUID) {
        writeLong(uuid.mostSignificantBits)
        writeLong(uuid.leastSignificantBits)
    }

    fun getByteBuffer(type: Class<out Packet?>?): ByteBuffer {
        val arr = (out as ByteArrayOutputStream).toByteArray()
        val out = ByteArray(arr.size + 1)
        out[0] = registeredPackets.getKey(type!!)!!
        System.arraycopy(arr, 0, out, 1, arr.size)
        return ByteBuffer.wrap(out)
    }
}
