package com.jeka8833.tntserverwebapi.websocket

import com.jeka8833.tntserverwebapi.websocket.packet.Packet
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class PacketListener {
    companion object {
        private val packetsListeners = ConcurrentHashMap<Int, SynchronousQueue<Packet>>()
        private val uniqueIdGenerator = AtomicInteger()

        fun sendGetResponse(packet: Packet): Packet? {
            val uniqueID = uniqueIdGenerator.incrementAndGet() and 0xFF_FF // Convert to short
            packet.setUniqueID(uniqueID)

            for (i in 0..1) {
                WebSocketClient.send(packet)

                val block = SynchronousQueue<Packet>()
                packetsListeners[uniqueID] = block

                return block.poll(5, TimeUnit.SECONDS) ?: continue
            }
            return null
        }

        fun callListener(packet: Packet) {
            val uniqueID = packet.getUniqueID()

            val queue = packetsListeners.remove(uniqueID) ?: return
            queue.offer(packet)
        }
    }
}