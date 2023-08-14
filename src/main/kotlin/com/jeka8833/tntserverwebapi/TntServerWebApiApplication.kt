package com.jeka8833.tntserverwebapi

import com.jeka8833.tntserverwebapi.database.UserPrivilegeRepository
import com.jeka8833.tntserverwebapi.git.GitFileController
import com.jeka8833.tntserverwebapi.websocket.WebSocketClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.nio.file.Paths

@Component
@SpringBootApplication
class TntServerWebApiApplication {

    @Value("\${git.url}")
    private lateinit var gitUrl: String

    @Value("\${git.project.path}")
    private lateinit var gitProjectPath: String

    @Value("\${websocket.url}")
    private lateinit var webSocketUrl: String

    @Autowired
    private lateinit var userPrivilegeRepository: UserPrivilegeRepository

    @EventListener(ApplicationReadyEvent::class)
    fun doSomethingAfterStartup() {
        GitFileController.init(gitUrl, Paths.get(gitProjectPath).toAbsolutePath())
        WebSocketClient.init(webSocketUrl, userPrivilegeRepository)
    }
}

fun main(args: Array<String>) {
    runApplication<TntServerWebApiApplication>(*args)
}
