package com.postgres.routing

import com.postgres.dto.request.request.UserDTO
import com.postgres.service.UserService
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting(userService: UserService) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        route("/users") {
            get {
                val result = userService.getAllUsers()
                call.respond(result)
            }
            post() {
                val user = call.receive<UserDTO>()
                val result = userService.createUser(user)
                call.respond(result)
            }
            get("/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                val result = userService.getUserById(id)
                call.respond(result)
            }
            put() {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val user = call.receive<UserDTO>()
                val result = userService.updateUser(user)
                call.respond(result)
            }
            delete("/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                val result = userService.deleteUser(id)
                call.respond(result)
            }
        }
    }
}