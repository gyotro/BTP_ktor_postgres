package com.postgres.db

import kotlinx.serialization.json.*

data class PgCreds(
    val host: String,
    val port: Int,
    val db: String,
    val user: String,
    val password: String,
    val sslRootCertPem: String? = null
)

fun resolveCredsFromVcap(instanceName: String = "postgreSQL-dev"): PgCreds {
    val vcap = System.getenv("VCAP_SERVICES")
        ?: error("VCAP_SERVICES not found. Are you running on Cloud Foundry?")

    val root = Json.parseToJsonElement(vcap).jsonObject
    val bindings = root["postgresql-db"]?.jsonArray
        ?: error("VCAP_SERVICES does not contain 'postgresql-db'")

    val binding = bindings
        .map { it.jsonObject }
        .firstOrNull { it["name"]?.jsonPrimitive?.content == instanceName }
        ?: error("No postgresql-db binding found with name='$instanceName'")

    val creds = binding["credentials"]!!.jsonObject

    fun str(key: String) = creds[key]?.jsonPrimitive?.content
        ?: error("Missing credential: $key")

    fun portAsInt(): Int {
        val p = creds["port"] ?: error("Missing credential: port")
        return when {
            p is JsonPrimitive && p.isString -> p.content.toInt()
            p is JsonPrimitive && !p.isString -> p.int
            else -> error("Unsupported port type")
        }
    }

    return PgCreds(
        user = str("username"),
        password = str("password"),
        host = str("hostname"),
        db = str("dbname"),
        port = portAsInt(),
        sslRootCertPem = creds["sslrootcert"]?.jsonPrimitive?.content
            ?: creds["sslcert"]?.jsonPrimitive?.content // fallback
    )
}
