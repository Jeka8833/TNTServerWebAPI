package com.jeka8833.tntserverwebapi.websocket

import com.jeka8833.tntserverwebapi.websocket.packet.Packet
import com.jeka8833.tntserverwebapi.websocket.packet.PacketInputStream
import com.jeka8833.tntserverwebapi.websocket.packet.PacketOutputStream
import com.jeka8833.tntserverwebapi.websocket.packet.packets.AuthApiPacket
import com.jeka8833.tntserverwebapi.websocket.packet.packets.BlockModulesPacket
import com.jeka8833.tntserverwebapi.websocket.packet.packets.ModulesStatusPacket
import com.jeka8833.tntserverwebapi.websocket.packet.packets.TokenGeneratorPacket
import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class WebSocketClient : WebSocketListener() {
    companion object {
        private lateinit var serverIp: String

        val registeredPackets: BiMap<Byte, Class<out Packet?>> = BiMap()

        private var logger: Logger = LoggerFactory.getLogger(WebSocketClient::class.java)
        private val client = OkHttpClient()

        init {
            registeredPackets.put(7.toByte(), BlockModulesPacket::class.java)
            registeredPackets.put(253.toByte(), TokenGeneratorPacket::class.java)
            registeredPackets.put(254.toByte(), ModulesStatusPacket::class.java)
            registeredPackets.put(255.toByte(), AuthApiPacket::class.java)
        }

        private var isConnect: Boolean = false
        private var timeReconnect: Long = 0

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

        fun init(webSocketUrl: String) {
            serverIp = webSocketUrl

            val thread = Thread {
                while (!Thread.interrupted()) {
                    try {
                        if (!isConnect && System.currentTimeMillis() > timeReconnect) {
                            connect()
                        }

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

        fun setStateConnected() {
            isConnect = true
        }

        private fun connect() {
            if (isConnect) return

            disconnect()

            val request: Request = Request.Builder().url(serverIp).build()

            wsClient = WebSocketClient()
            socket = client.newWebSocket(request, wsClient!!)

            timeReconnect = System.currentTimeMillis() + 10_000
        }

        private fun disconnect() {
            isConnect = false

            socket?.close(1000, "Force close")
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        send(AuthApiPacket())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        isConnect = false
        logger.warn("WebSocket close: $code -> $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        isConnect = false
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