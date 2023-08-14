package com.jeka8833.tntserverwebapi.git

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.logging.log4j.LogManager
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URL
import java.util.*
import java.util.function.Consumer
import javax.imageio.ImageIO
import kotlin.math.min

class PlayerCustomization {
    companion object {
        private val logger = LogManager.getLogger(PlayerCustomization::class.java)
        private val JSON: ObjectMapper = ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)

        fun editCape(uuid: UUID, data: String, useTntCape: Boolean): Int {
            val capeImageFile = GitFileController.getProjectFolder().resolve("capes/$uuid.png").toFile()
            val capeSettingsFile = GitFileController.getProjectFolder().resolve("capeData/$uuid.json").toFile()

            var errorCode = 0

            GitFileController.addTask {
                if (!writeImageToFile(capeImageFile, data)) {
                    errorCode = 101
                    return@addTask
                }

                if (!editConfiguration(capeSettingsFile) { config ->
                        config.setUseTNTCape(useTntCape)
                    }) {
                    errorCode = 102
                    return@addTask
                }
            }

            return errorCode
        }

        fun editHeart(uuid: UUID, textAnimation: Array<String>, delayTime: Int): Int {
            val capeSettingsFile = GitFileController.getProjectFolder().resolve("capeData/$uuid.json").toFile()
            var errorCode = 0
/*
            GitFileController.addTask {
                if (!editConfiguration(capeSettingsFile) { config ->
                        config.setTextAnimation(textAnimation)
                        config.setDelayAnimation(delayTime)
                    }) {
                    errorCode = 102
                    return@addTask
                }
            }*/
            return errorCode
        }

        private fun editConfiguration(path: File, callback: Consumer<PlayerCustomizationPreferences>): Boolean {
            try {
                val config: PlayerCustomizationPreferences = if (!path.exists()) {
                    PlayerCustomizationPreferences()
                } else {
                    JSON.readValue(path, PlayerCustomizationPreferences::class.java)
                }

                callback.accept(config)
                JSON.writeValue(path, config)
                return true

            } catch (_: Exception) {
            }
            return false
        }

        private fun writeImageToFile(path: File, data: String): Boolean {
            if (data.isBlank()) return true
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

                newWidth = min(originalImage!!.width, 22 * (newWidth / 64))
                newHeight = min(originalImage!!.height, 17 * (newHeight / 32))

                val croppedCape = originalImage!!.getSubimage(0, 0, newWidth, newHeight)
                ImageIO.write(croppedCape, "png", path)

                return true
            } catch (e: Exception) {
                logger.warn("Fail read image", e)
            }
            return false
        }
    }
}