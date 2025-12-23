package com.postgres.db

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

    fun fromEnv(serviceInstanceName: String): DbCreds {
        log.info("Resolving database credentials (service=$serviceInstanceName)")
        val vcap = System.getenv("VCAP_SERVICES")
        if (!vcap.isNullOrBlank()) {
            val root = Json.parseToJsonElement(vcap).jsonObject
            val services = root["postgresql-db"]?.jsonArray
                ?: error("VCAP_SERVICES present, but no 'postgresql-db' entry found")

            val match = services
                .map { it.jsonObject }
                .firstOrNull { it["name"]?.jsonPrimitive?.content == serviceInstanceName }
                ?: services.first().jsonObject // fallback: first binding

            val creds = match["credentials"]!!.jsonObject

            return DbCreds(
                host = creds["hostname"]!!.jsonPrimitive.content,
                port = creds["port"]!!.jsonPrimitive.content.toInt(),
                dbName = creds["dbname"]!!.jsonPrimitive.content,
                username = creds["username"]!!.jsonPrimitive.content,
                password = creds["password"]!!.jsonPrimitive.content,
                sslRootCertPem = creds["sslrootcert"]?.jsonPrimitive?.content
            )
        }

        // Local fallback (example)
        return DbCreds(
            host = System.getenv("DB_HOST") ?: "localhost",
            port = (System.getenv("DB_PORT") ?: "5432").toInt(),
            dbName = System.getenv("DB_NAME") ?: "postgres",
            username = System.getenv("DB_USER") ?: "postgres",
            password = System.getenv("DB_PASS") ?: "postgres",
            sslRootCertPem = null
        )
    }

    fun hikariDataSource(creds: DbCreds): HikariDataSource {

        val sslMode = System.getenv("PG_SSLMODE") ?: "verify-full"

        val maxPool = (System.getenv("DB_POOL_MAX") ?: "10").toInt()
        val minIdle = (System.getenv("DB_POOL_MIN") ?: "2").toInt()
        val connTimeoutMs = (System.getenv("DB_POOL_CONN_TIMEOUT_MS") ?: "10000").toLong()
        val idleTimeoutMs = (System.getenv("DB_POOL_IDLE_TIMEOUT_MS") ?: "600000").toLong()
        val maxLifetimeMs = (System.getenv("DB_POOL_MAX_LIFETIME_MS") ?: "1800000").toLong()

        log.info(
            "Creating HikariDataSource: host={}, port={}, db={}, sslMode={}, maxPool={}",
            creds.host,
            creds.port,
            creds.dbName,
            sslMode,
            maxPool
        )

        val sslRootCertPath = creds.sslRootCertPem?.let { pem ->
            val certFile = File("/tmp/btp-pg-root.crt")
            certFile.writeText(pem)
            certFile.absoluteFile
        }

        val jdbcPostgresUrl = buildString {
            append("jdbc:postgresql://${creds.host}:${creds.port}/${creds.dbName}")
            append("?sslmode=$sslMode")

            if (sslRootCertPath != null) {
                append("&sslrootcert=${sslRootCertPath.absolutePath}")
            }

            // Optional but recommended on CF
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

        cfg.validate()
        return HikariDataSource(cfg)
    }

}
