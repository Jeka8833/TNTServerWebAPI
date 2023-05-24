package com.jeka8833.tntserverwebapi.git

import org.apache.logging.log4j.LogManager
import java.nio.file.Path
import java.time.ZoneId
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque


class GitFileController {
    companion object {
        private val logger = LogManager.getLogger(GitManager::class.java)
        private val changedFilesTasks: BlockingQueue<Runnable> = LinkedBlockingDeque()

        private lateinit var projectFolder: Path
        private lateinit var gitManager: GitManager
        private var thread: Thread? = null

        fun init(urlWithAuth: String, projectFolder: Path) {
            if (thread != null) return

            thread = Thread({
                if (gitManager.isBehindBranch) gitManager.pull()    // You may lose old files

                var errorCount = 0
                while (!Thread.currentThread().isInterrupted) {
                    try {
                        for (i in 0..1) {
                            if (gitManager.push("[ServerAPI] Update files. Current time: [time]")) break

                            Thread.sleep(5000)

                            if (i == 1) {
                                errorCount++
                                if (errorCount > 15) {
                                    gitManager.pull()      // Force delete edited files
                                    errorCount = 0
                                }

                                throw NullPointerException("Git push operation continue with error")
                            }
                        }
                        errorCount = 0

                        var task: Runnable? = changedFilesTasks.take()  // Block thread

                        for (i in 0..1) {
                            if (gitManager.pull()) break

                            Thread.sleep(5000)

                            if (i == 1) {
                                if (task != null) changedFilesTasks.put(task)
                                throw NullPointerException("Git pull operation continue with error")
                            }
                        }

                        while (task != null) {
                            task.run()
                            task = changedFilesTasks.poll()
                        }
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                        break
                    } catch (e: Exception) {
                        logger.warn("GitFileController has an error", e)
                        Thread.sleep(5000)
                    }
                }
            }, "Git controller thread")
            Companion.projectFolder = projectFolder

            gitManager = GitManager(urlWithAuth, projectFolder)
            gitManager.timeZone = ZoneId.of("Europe/Kiev")
            gitManager.setDeleteBeforePull(false)
            gitManager.setDeleteAfterPush(false)

            thread!!.priority = Thread.MIN_PRIORITY
            thread!!.isDaemon = true
            thread!!.start()
        }

        fun addTask(task: Runnable) {
            changedFilesTasks.put(task)
        }

        fun getProjectFolder(): Path {
            return projectFolder
        }
    }
}