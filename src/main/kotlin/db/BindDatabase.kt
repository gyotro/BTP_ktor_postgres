package com.postgres.db

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.ext.inject

fun Application.configureDatabase() {

    val db by inject<Database>()
    val userServiceDb by inject<UserServiceDB>()
}
