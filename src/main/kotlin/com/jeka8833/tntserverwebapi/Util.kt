package com.jeka8833.tntserverwebapi

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.logging.log4j.LogManager
import org.jetbrains.annotations.Contract
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.net.URL
import java.nio.file.Path
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.min


class Util {
    companion object {
        private val logger = LogManager.getLogger(Util::class.java)

        var JSON: ObjectMapper = ObjectMapper()

        @Contract("null -> null")
        fun parseUUID(text: String?) = runCatching { UUID.fromString(text) }.getOrNull()

        @Contract("null -> null")
        fun parsePlayerUUID(text: String?): UUID? {
            val player = parseUUID(text) ?: return null

            if (player.version() == 4 && player.variant() == 2) return player
            return null
        }

        val platform: OS
            get() {
                val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
                if (osName.contains("win")) {
                    return OS.WINDOWS
                }
                if (osName.contains("mac")) {
                    return OS.MACOS
                }
                if (osName.contains("solaris")) {
                    return OS.SOLARIS
                }
                if (osName.contains("sunos")) {
                    return OS.SOLARIS
                }
                if (osName.contains("linux")) {
                    return OS.LINUX
                }
                return if (osName.contains("unix")) {
                    OS.LINUX
                } else OS.UNKNOWN
            }

        fun writeImageToFile(path: Path, data: String): Boolean {
            try {
                var originalImage: BufferedImage? = null
                if (data.startsWith("data:image/png;base64,") || data.startsWith("data:image/jpg;base64,")
                    || data.startsWith("data:image/jpeg;base64,")
                ) {
                    val base64Image = data.split(",")[1]
                    ByteArrayInputStream(Base64.getDecoder().decode(base64Image)).use { stream ->
                        originalImage = ImageIO.read(stream)
                    }
                } else if (data.startsWith("http")) {
                    originalImage = ImageIO.read(URL(data))
                }

                if (originalImage == null) return false

                var newWidth = 64
                var newHeight = 32
                while ((newWidth < originalImage!!.width || newHeight < originalImage!!.height) && newWidth < 1024) {
                    newWidth *= 2
                    newHeight *= 2
                }

                newWidth = min(originalImage!!.width, 46 * (newWidth / 64))
                newHeight = min(originalImage!!.height, 22 * (newHeight / 32))

                val croppedCape = originalImage!!.getSubimage(0, 0, newWidth, newHeight)
                ImageIO.write(croppedCape, "png", path.toFile())

                return true
            } catch (e: Exception) {
                logger.warn("Fail read image", e)
            }
            return false
        }
    }

    enum class OS {
        LINUX,
        SOLARIS,
        WINDOWS,
        MACOS,
        UNKNOWN
    }
}