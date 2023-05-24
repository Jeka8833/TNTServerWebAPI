package com.jeka8833.tntserverwebapi

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.logging.log4j.LogManager
import org.jetbrains.annotations.Contract
import java.io.ByteArrayInputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.imageio.ImageIO
import javax.imageio.ImageReader

class Util {
    companion object {
        private const val limitFileSize = 128 * 1024 // 128 KB

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
                var image: ByteArray? = null
                if (data.startsWith("data:image/png;base64,") || data.startsWith("data:image/jpg;base64,")
                    || data.startsWith("data:image/jpeg;base64,")
                ) {
                    val base64Image = data.split(",")[1]
                    image = Base64.getDecoder().decode(base64Image)
                } else if (data.startsWith("http")) {
                    image = URL(data).readBytes()
                }

                if (image == null) return false

                var isPNG = false
                val iis = ImageIO.createImageInputStream(ByteArrayInputStream(image))
                val imageReaders: Iterator<ImageReader> = ImageIO.getImageReaders(iis)

                while (imageReaders.hasNext()) {
                    val reader: ImageReader = imageReaders.next()
                    isPNG = reader.formatName.equals("png", true)
                }

                val processedImage = ImageIO.read(ByteArrayInputStream(image))  // Always validate image
                if (!isPNG || image.size > limitFileSize) {
                    ImageIO.write(processedImage, "png", path.toFile())
                } else {
                    Files.write(path, image)
                }
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