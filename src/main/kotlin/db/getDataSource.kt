package com.postgres.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

fun hikariDataSource(instanceName: String = "postgreSQL-dev"): HikariDataSource {
    val c = resolveCredsFromVcap(instanceName)

    val sslRootCertPath: Path? = c.sslRootCertPem?.let { pem ->
        val p = Files.createTempFile("pg-root-", ".pem")
        p.toFile().deleteOnExit()
        p.writeText(pem)
        p
    }

    val jdbcUrl = buildString {
        append("jdbc:postgresql://${c.host}:${c.port}/${c.db}")
        append("?sslmode=verify-full")
        if (sslRootCertPath != null) {
            append("&sslrootcert=${sslRootCertPath.toAbsolutePath()}")
        }
        // If you ever need it: &tcpKeepAlive=true
    }

    val cfg = HikariConfig().apply {
        this.jdbcUrl = jdbcUrl
        this.username = c.user
        this.password = c.password
        this.driverClassName = "org.postgresql.Driver"

        maximumPoolSize = (System.getenv("DB_POOL_MAX") ?: "10").toInt()
        minimumIdle = (System.getenv("DB_POOL_MIN") ?: "1").toInt()
        connectionTimeout = 10_000
        idleTimeout = 60_000
        maxLifetime = 25 * 60_000
    }

    return HikariDataSource(cfg)
}
