package com.postgres.routing

import com.postgres.dto.request.request.UserDTO
import com.postgres.dto.request.respose.ApiResult
import com.postgres.dto.request.respose.respondEither
import com.postgres.dto.request.respose.respondUserError
import com.postgres.service.UserService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Application.configureRouting() {
    val userService by inject<UserService>()

    routing {
        authenticate("xsuaa") {

            // Swagger UI at /swagger
            //swaggerUI(path = "/swagger", swaggerFile = "swagger/documentation.yml")
            //openAPI(path = "/openapi", swaggerFile = "swagger/documentation.yml")

            get("/") {
                call.respondText("Hello World!")
            }

            route("/users") {

                // GET /users
                get {
                    call.respondEither(userService.getAllUsers())
                }

                // POST /users  -> 201 + Location: /users/{id}
                post {
                    val dto = call.receive<UserDTO>()

                    userService.createUser(dto).fold(
                        { err ->
                            call.respondUserError(err)
                        },
                        { newId ->
                            val location = "/users/$newId"
                            call.response.header(HttpHeaders.Location, location)

                            call.respond(
                                HttpStatusCode.Created,
                                ApiResult.Ok(
                                    data = mapOf("id" to newId.toString()),
                                    message = "User created"
                                )
                            )
                        }
                    )
                }

                // GET /users/{id}
                get("/{id}") {
                    val id = call.parameters["id"].orEmpty()
                    call.respondEither(userService.getUserById(id))
                }

                // PUT /users/{id}
                put("/{id}") {
                    val id = call.parameters["id"].orEmpty()
                    val dto = call.receive<UserDTO>()

                    // Avoid mutating DTO if possible; better to create a copy.
                    val updated = dto.copy(id = UUID.fromString(id))

                    call.respondEither(userService.updateUser(updated), HttpStatusCode.NoContent)
                    // You may prefer HttpStatusCode.NoContent here; see note below.
                }

                // DELETE /users/{id}
                delete("/{id}") {
                    val id = call.parameters["id"].orEmpty()

                    userService.deleteUser(id).fold(
                        { err -> call.respondUserError(err) },
                        {
                            // Common REST choice: 204 No Content on successful delete
                            call.respond(HttpStatusCode.NoContent)
                        }
                    )
                }
            }
        }
    }
}
