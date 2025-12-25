package com.postgres.db

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.ext.inject

fun Application.configureDatabase() {
/*    val creds = DbConfig.fromEnv(serviceInstanceName = "postgreSQL-dev")
    val ds = DbConfig.hikariDataSource(creds)

    val db = Database.connect(ds)

    // For quick start; replace with migrations later
*//*   transaction {
        SchemaUtils.createMissingTablesAndColumns(UsersTable)
    }

    UserServiceDB(db)*/
    // Resolve singletons to ensure DB connection is created at startup
    val db by inject<Database>()
    val userServiceDb by inject<UserServiceDB>()
}
