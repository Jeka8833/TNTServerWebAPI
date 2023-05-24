package com.jeka8833.tntserverwebapi.git

import com.jeka8833.tntserverwebapi.Util
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.jetbrains.annotations.Contract
import java.io.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future

class Application(val process: Process) {
    private var infoThread: Future<String>? = null
    private var errorThread: Future<String>? = null

    @Throws(InterruptedException::class)
    fun waitEnd() {
        process.waitFor()
    }

    fun forceStop() {
        process.destroyForcibly()
        infoThread?.cancel(true)
        errorThread?.cancel(true)
    }

    private fun setInfoThread(infoThread: Future<String>?) {
        this.infoThread = infoThread
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun getInfoLog(): String? {
        if (infoThread == null) return null
        return infoThread!!.get()
    }

    private fun setErrorThread(errorThread: Future<String>?) {
        this.errorThread = errorThread
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun getErrorLog(): String? {
        if (errorThread == null) return null
        return errorThread!!.get()
    }

    companion object {
        private val logger = LogManager.getLogger(Application::class.java)
        private val THREAD_POOL = Executors.newCachedThreadPool { r: Runnable ->
            val t = Executors.defaultThreadFactory().newThread(r)
            t.isDaemon = true
            t
        }

        @Throws(IOException::class)
        fun createProcessAndLogging(
            consolePrefix: String, workDirectory: File?,
            vararg command: String?,
        ): Application {
            val application = createProcess(workDirectory, *command)
            application.setInfoThread(
                logStreamAndAddListener(
                    application.process.inputStream, Level.INFO, consolePrefix
                )
            )
            application.setErrorThread(
                logStreamAndAddListener(
                    application.process.errorStream, Level.ERROR, consolePrefix
                )
            )
            return application
        }

        @Contract("_, _, _ -> new")
        fun logStreamAndAddListener(stream: InputStream, level: Level, prefix: String): Future<String> {
            return THREAD_POOL.submit<String> {
                val stringBuilder = StringBuilder()
                try {
                    BufferedReader(InputStreamReader(stream))
                        .forEachLine { line: String ->
                            logger.log(level, prefix + line)
                            stringBuilder.append(line).append('\n')
                        }
                } catch (e: Exception) {
                    logger.error("Fail open or close logger stream", e)
                }
                stringBuilder.toString()
            }
        }

        @Throws(IOException::class)
        fun createProcess(workDirectory: File?, vararg command: String?): Application {
            val outArgs = when (Util.platform) {
                Util.OS.WINDOWS -> arrayOf("cmd.exe", "/C", java.lang.String.join(" ", *command))
                Util.OS.LINUX -> arrayOf("/bin/bash", "-c", java.lang.String.join(" ", *command))
                else -> command
            }
            val builder = ProcessBuilder(*outArgs)
            builder.directory(workDirectory)
            return Application(builder.start())
        }
    }
}
