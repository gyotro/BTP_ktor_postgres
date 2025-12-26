package com.postgres.di

import arrow.core.getOrElse
import com.postgres.db.*
import com.postgres.repository.*
import com.postgres.service.UserService
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

val appModule = module {

    single<DbCreds> {
        val serviceName = System.getenv("DB_SERVICE_NAME") ?: "postgreSQL-dev"

        DbConfig.fromEnv(serviceInstanceName = serviceName)
            .getOrElse { err ->
                // Koin boundary: if DB config fails, crash startup with a useful error
                throw IllegalStateException("Failed to resolve DB credentials: $err")
            }
    }

    single<HikariDataSource> {
        DbConfig.hikariDataSource(get())
            .getOrElse { err ->
                throw IllegalStateException("Failed to create HikariDataSource: $err")
            }
    }

    single<Database> {
        Database.connect(get<HikariDataSource>())
    }

    single<UserServiceDB> {
        val userSchema = UserServiceDB(get())
        userSchema.ensureSchema().getOrElse { err ->
            throw IllegalStateException("DB schema init failed: $err")
        }
        userSchema
    }
    single<UserRepo> { UserRepoImpl(get()) }
    single<UserService> { UserService(get()) }
}
