package com.jeka8833.tntserverwebapi

import org.jetbrains.annotations.Contract
import java.util.*
import java.util.regex.Pattern


class Util {
    companion object {
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
        const val COLOR_CHAR = '\u00A7'
        private val STRIP_COLOR_PATTERN: Pattern = Pattern.compile("(?i)$COLOR_CHAR[0-9A-FK-OR]")

        /**
         * Strips the given message of all color codes
         *
         * @param input String to strip of color
         * @return A copy of the input string, without any coloring
         */
        fun stripColor(input: String?): String? {
            return if (input == null) {
                null
            } else STRIP_COLOR_PATTERN.matcher(input).replaceAll("")
        }

        /**
         * Translates a string using an alternate color code character into a
         * string that uses the internal ChatColor.COLOR_CODE color code
         * character. The alternate color code character will only be replaced if
         * it is immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
         *
         * @param altColorChar The alternate color code character to replace. Ex: &
         * @param textToTranslate Text containing the alternate color code character.
         * @return Text containing the ChatColor.COLOR_CODE color code character.
         */
        fun translateAlternateColorCodes(altColorChar: Char, textToTranslate: String): String {
            val b = textToTranslate.toCharArray()
            for (i in 0 until b.size - 1) {
                if ((b[i] == altColorChar || b[i] == COLOR_CHAR) &&
                    "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1
                ) {
                    b[i] = COLOR_CHAR
                    b[i + 1] = b[i + 1].lowercaseChar()
                }
            }
            return String(b)
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