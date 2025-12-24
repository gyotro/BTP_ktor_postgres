package com.postgres

import com.postgres.routing.configureRouting
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import com.postgres.di.appModule
import io.ktor.server.application.ApplicationStopped


fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    val log = environment.log

    log.info("=== Application startup ===")
    log.info("App name: ${System.getenv("VCAP_APPLICATION")?.let { "CF app" } ?: "local"}")
    log.info("PORT env var = ${System.getenv("PORT")}")
    log.info("Running on Cloud Foundry = ${System.getenv("VCAP_SERVICES") != null}")


    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }

    // Fail fast: DB connect + SchemaUtils in UserServiceDB.init{}
    getKoin().get<UserServiceDB>()

    // Close Hikari on shutdown
    monitor.subscribe(ApplicationStopped) {
        getKoin().get<HikariDataSource>().close()
    }

    configureSerialization()
    configureMonitoring()
    configureErrors()
    configureRouting()

}
