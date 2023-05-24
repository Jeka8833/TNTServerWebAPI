package com.jeka8833.tntserverwebapi.git

import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class GitManager(urlWithAuth: String?, projectFolder: Path) {
    private val urlWithAuth: String
    private val projectFolder: Path

    var timeZone: ZoneId = ZoneId.systemDefault()
        set(value) {
            field = value
        }

    private var deleteAfterPush = true
    private var deleteBeforePull = true

    init {
        require(!urlWithAuth.isNullOrBlank()) { "Git URL is null or blank" }
        this.urlWithAuth = urlWithAuth
        this.projectFolder = projectFolder
    }

    fun pull(): Boolean {
        try {
            if (deleteBeforePull || !isCurrentBranch) {
                deleteFolder(projectFolder)
                val gitProcess: Application = Application.createProcessAndLogging(
                    "<git-clone> ", null,
                    "git", "clone", urlWithAuth, projectFolder.toString()
                )
                gitProcess.waitEnd()
            }
            return true
        } catch (e: Exception) {
            logger.error("Fail pull files", e)
        }
        return false
    }

    fun push(commitName: String): Boolean {
        if (!Files.isDirectory(projectFolder)) return false
        try {
            val addProcess: Application = Application.createProcessAndLogging(
                "<git-add> ", projectFolder.toFile(), "git", "add", "*"
            )
            addProcess.waitEnd()
            val commitProcess: Application = Application.createProcessAndLogging(
                "<git-commit> ", projectFolder.toFile(),
                "git", "commit", "-m",
                '"'.toString() + commitName.replace(
                    "[time]",
                    ZonedDateTime.now(timeZone).format(formatter)
                ) + '"'
            )
            commitProcess.waitEnd()
            val pushProcess: Application = Application.createProcessAndLogging(
                "<git-push> ", projectFolder.toFile(),
                "git", "push", "--force", urlWithAuth
            )
            pushProcess.waitEnd()
            if (deleteAfterPush) deleteFolder(projectFolder)
            return true
        } catch (e: Exception) {
            logger.error("Fail push files", e)
        }
        return false
    }

    val isBehindBranch: Boolean
        get() {
            if (!Files.isDirectory(projectFolder)) return true
            try {
                val updateProcess: Application = Application.createProcessAndLogging(
                    "<git-remote> ", projectFolder.toFile(), "git", "remote", "update"
                )
                updateProcess.waitEnd()
                val statusProcess: Application = Application.createProcessAndLogging(
                    "<git-status> ", projectFolder.toFile(), "git", "status", "-uno"
                )
                val logText = statusProcess.getInfoLog() ?: return true
                return logText.contains("Your branch is behind")
            } catch (e: Exception) {
                logger.error("Fail check branch", e)
            }
            return true
        }

    private val isCurrentBranch: Boolean
        get() {
            if (!Files.isDirectory(projectFolder)) return false
            try {
                val updateProcess: Application = Application.createProcessAndLogging(
                    "<git-remote> ", projectFolder.toFile(), "git", "remote", "update"
                )
                updateProcess.waitEnd()
                val statusProcess: Application = Application.createProcessAndLogging(
                    "<git-status> ", projectFolder.toFile(), "git", "status", "-uno"
                )
                val logText = statusProcess.getInfoLog() ?: return false
                return logText.contains("Your branch is up to date with")
            } catch (e: Exception) {
                logger.error("Fail check branch", e)
            }
            return false
        }

    fun setDeleteBeforePull(deleteBeforePull: Boolean) {
        this.deleteBeforePull = deleteBeforePull
    }

    fun setDeleteAfterPush(deleteAfterPush: Boolean) {
        this.deleteAfterPush = deleteAfterPush
    }

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("MM.dd.yyyy HH:mmZ")
        private val logger = LogManager.getLogger(GitManager::class.java)
        private fun deleteFolder(folder: Path) {
            if (Files.isDirectory(folder)) {
                try {
                    Files.walk(folder).use { pathStream ->
                        pathStream.sorted(Comparator.reverseOrder())
                            .map { obj: Path -> obj.toFile() }
                            .forEach { obj: File -> obj.delete() }
                    }
                } catch (e: Exception) {
                    logger.error("Fail delete folder", e)
                }
            }
        }
    }
}