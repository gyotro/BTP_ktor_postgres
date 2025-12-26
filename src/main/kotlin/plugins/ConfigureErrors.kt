package com.postgres.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException

fun Application.configureErrors() {
    val log = environment.log
    install(StatusPages) {
        exception<SerializationException> { call, cause ->
            log.error("JSON serialization error", cause)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON", "details" to (cause.message ?: "")))
        }
        exception<Exception> { call, cause ->
            log.error("Unhandled error", cause)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Server error"))
        }
    }
}
