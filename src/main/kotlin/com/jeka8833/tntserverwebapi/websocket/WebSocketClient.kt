package com.jeka8833.tntserverwebapi.websocket

import com.jeka8833.tntserverwebapi.database.UserPrivilegeRepository
import com.jeka8833.tntserverwebapi.websocket.packet.Packet
import com.jeka8833.tntserverwebapi.websocket.packet.PacketInputStream
import com.jeka8833.tntserverwebapi.websocket.packet.PacketOutputStream
import com.jeka8833.tntserverwebapi.websocket.packet.packets.*
import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class WebSocketClient : WebSocketListener() {
    companion object {
        private lateinit var serverIp: String
        lateinit var privilegeRepository: UserPrivilegeRepository

        val registeredPackets: BiMap<Byte, Class<out Packet?>> = BiMap()

        private var logger: Logger = LoggerFactory.getLogger(WebSocketClient::class.java)
        private val client = OkHttpClient()

        init {
            registeredPackets.put(3.toByte(), PingPacket::class.java)
            registeredPackets.put(7.toByte(), BlockModulesPacket::class.java)
            registeredPackets.put(251.toByte(), RolePacket::class.java)
            registeredPackets.put(253.toByte(), TokenGeneratorPacket::class.java)
            registeredPackets.put(254.toByte(), ModulesStatusPacket::class.java)
            registeredPackets.put(255.toByte(), AuthApiPacket::class.java)
        }

        var timeReconnect: Long = 0
        private var pingAt: Long = Long.MAX_VALUE

        private var wsClient: WebSocketClient? = null
        private var socket: WebSocket? = null

        fun send(packet: Packet): Boolean {
            try {
                PacketOutputStream().use { stream ->
                    packet.write(stream)
                    socket!!.send(stream.getByteBuffer(packet.javaClass).toByteString())
                    return true
                }
            } catch (e: Throwable) {
                logger.error("WebSocket fail send: ", e)
            }
            return false
        }

        fun init(webSocketUrl: String, privilegeRepository: UserPrivilegeRepository) {
            serverIp = webSocketUrl
            this.privilegeRepository = privilegeRepository

            val thread = Thread {
                while (!Thread.interrupted()) {
                    try {
                        if (System.currentTimeMillis() > pingAt) {
                            send(PingPacket())
                            pingAt = System.currentTimeMillis() + 15_000
                        }

                        connect()

                        Thread.sleep(5_000)
                    } catch (i: InterruptedException) {
                        throw i
                    } catch (e: Exception) {
                        logger.error("WebSocket tick demon had error", e)
                    }
                }
            }
            thread.priority = Thread.MIN_PRIORITY
            thread.isDaemon = true
            thread.start()
        }

        private fun connect() {
            if (System.currentTimeMillis() < timeReconnect) return

            disconnect()

            val request: Request = Request.Builder().url(serverIp).build()

            wsClient = WebSocketClient()
            socket = client.newWebSocket(request, wsClient!!)

            timeReconnect = System.currentTimeMillis() + 60_000L
            pingAt = System.currentTimeMillis() + 30_000L

            logger.info("WebSocket connected")
        }

        private fun disconnect() {
            socket?.close(1000, "Force close")
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        send(AuthApiPacket())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        logger.warn("WebSocket close: $code -> $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logger.warn("WebSocket close", t)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        try {
            PacketInputStream(bytes.toByteArray()).use { stream ->
                stream.packet.read(stream)
                stream.packet.clientProcess(webSocket)
            }
        } catch (e: java.lang.Exception) {
            logger.warn("WebSocket message receive error: ", e)
        }
    }
}