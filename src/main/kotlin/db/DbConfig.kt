package com.postgres.db

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.postgres.errors.DbError
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import java.io.File

data class DbCreds(
    val host: String,
    val port: Int,
    val dbName: String,
    val username: String,
    val password: String,
    val sslRootCertPem: String? = null,
)

object DbConfig {

    private val log = LoggerFactory.getLogger(DbConfig::class.java)

    fun fromEnv(serviceInstanceName: String): Either<DbError, DbCreds> = either {
        log.info("Resolving database credentials (service={})", serviceInstanceName)

        val vcap = System.getenv("VCAP_SERVICES")

        if (!vcap.isNullOrBlank()) {
            val root = try {
                Json.parseToJsonElement(vcap).jsonObject
            } catch (t: Throwable) {
                raise(DbError.InvalidVcap("VCAP_SERVICES is not valid JSON", t))
            }

            val services = root["postgresql-db"]?.jsonArray
                ?: raise(DbError.MissingVcapEntry("postgresql-db"))

            val match = services
                .map { it.jsonObject }
                .firstOrNull { it["name"]?.jsonPrimitive?.content == serviceInstanceName }
                ?: services.firstOrNull()?.jsonObject
                ?: raise(DbError.InvalidVcap("postgresql-db array is empty"))

            val creds = match["credentials"]?.jsonObject
                ?: raise(DbError.InvalidVcap("Missing credentials object for postgresql-db binding"))

            fun s(key: String): String =
                creds[key]?.jsonPrimitive?.content
                    ?: raise(DbError.MissingCredential(key))

            fun int(key: String): Int {
                val raw = s(key)
                return raw.toIntOrNull() ?: raise(DbError.BadCredential(key, raw))
            }

            return@either DbCreds(
                host = s("hostname"),
                port = int("port"),
                dbName = s("dbname"),
                username = s("username"),
                password = s("password"),
                sslRootCertPem = creds["sslrootcert"]?.jsonPrimitive?.content
            )
        }
        else {
            raise(DbError.MissingEnv("VCAP_SERVICES"))
        }
    }

    fun hikariDataSource(creds: DbCreds): Either<DbError, HikariDataSource> = either {
        val sslMode = System.getenv("PG_SSLMODE") ?: "verify-full"

        fun envInt(name: String, default: Int): Int {
            val raw = System.getenv(name) ?: default.toString()
            return raw.toIntOrNull() ?: raise(DbError.BadCredential(name, raw))
        }

        fun envLong(name: String, default: Long): Long {
            val raw = System.getenv(name) ?: default.toString()
            return raw.toLongOrNull() ?: raise(DbError.BadCredential(name, raw))
        }

        val maxPool = envInt("DB_POOL_MAX", 10)
        val minIdle = envInt("DB_POOL_MIN", 2)
        val connTimeoutMs = envLong("DB_POOL_CONN_TIMEOUT_MS", 10_000)
        val idleTimeoutMs = envLong("DB_POOL_IDLE_TIMEOUT_MS", 600_000)
        val maxLifetimeMs = envLong("DB_POOL_MAX_LIFETIME_MS", 1_800_000)

        ensure(maxPool >= 1) { DbError.BadCredential("DB_POOL_MAX", maxPool.toString()) }
        ensure(minIdle >= 0) { DbError.BadCredential("DB_POOL_MIN", minIdle.toString()) }

        log.info(
            "Creating HikariDataSource: host={}, port={}, db={}, sslMode={}, maxPool={}",
            creds.host, creds.port, creds.dbName, sslMode, maxPool
        )

        val sslRootCertPath = creds.sslRootCertPem?.let { pem ->
            try {
                val certFile = File("/tmp/btp-pg-root.crt")
                certFile.writeText(pem)
                certFile.absoluteFile
            } catch (t: Throwable) {
                raise(DbError.Io("Failed writing sslrootcert to /tmp", t))
            }
        }

        val jdbcPostgresUrl = buildString {
            append("jdbc:postgresql://${creds.host}:${creds.port}/${creds.dbName}")
            append("?sslmode=$sslMode")
            if (sslRootCertPath != null) {
                append("&sslrootcert=${sslRootCertPath.absolutePath}")
            }
            append("&tcpKeepAlive=true")
        }

        val cfg = HikariConfig().apply {
            jdbcUrl = jdbcPostgresUrl
            username = creds.username
            password = creds.password
            driverClassName = "org.postgresql.Driver"

            maximumPoolSize = maxPool
            minimumIdle = minIdle
            connectionTimeout = connTimeoutMs
            idleTimeout = idleTimeoutMs
            maxLifetime = maxLifetimeMs

            isAutoCommit = false
            transactionIsolation = "TRANSACTION_READ_COMMITTED"
        }

        try {
            cfg.validate()
            HikariDataSource(cfg)
        } catch (t: Throwable) {
            raise(DbError.Hikari("Failed creating/validating HikariDataSource", t))
        }
    }
}
