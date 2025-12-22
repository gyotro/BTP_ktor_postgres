package com.postgres

import com.postgres.routing.configureRouting
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
//    configureFrameworks()
//    configureSecurity()
    configureMonitoring()
//    configureSerialization()
    configureDatabases()
    configureRouting()
}
