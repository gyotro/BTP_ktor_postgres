package com.postgres.di

import com.postgres.UserServiceDB
import com.postgres.db.DbConfig
import com.postgres.db.DbCreds
import com.postgres.repository.UserRepo
import com.postgres.repository.UserRepoImpl
import com.postgres.service.UserService
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

val appModule = module {

    // 1️⃣ Database credentials
    // CF-first via VCAP_SERVICES, fallback to local env
    single<DbCreds> {
        val serviceName = System.getenv("DB_SERVICE_NAME") ?: "postgreSQL-dev"
        DbConfig.fromEnv(serviceInstanceName = serviceName)
    }

    // 2️⃣ HikariCP DataSource (singleton pool)
    single<HikariDataSource> {
        DbConfig.hikariDataSource(get())
    }

    // 3️⃣ Exposed Database (connected once)
    single<Database> {
        Database.connect(get<HikariDataSource>())
    }

    // 4️⃣ Your Exposed service
    // Schema creation happens in init { } as you wrote
    single<UserServiceDB> {
        UserServiceDB(get())
    }
    single<UserRepo> {
        UserRepoImpl(get())
    }
    single<UserService> {
        UserService(get())
    }
}
