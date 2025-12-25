package com.postgres.security

import kotlinx.serialization.json.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class XsuaaCredentials(
    val url: String,
    val xsappname: String,
    val clientid: String,
    val identityzone: String? = null,
    val identityzoneid: String? = null
)

fun readXsuaaCredentialsFromVcap(): XsuaaCredentials {
    val log: Logger = LoggerFactory.getLogger("SecurityConfig")
    val vcap = System.getenv("VCAP_SERVICES")
        ?: error("VCAP_SERVICES not found. Are you running on Cloud Foundry / with correct env?")

    if (vcap.isNotBlank()) {
        val root = Json.parseToJsonElement(vcap).jsonObject
        val xsuaa = root["xsuaa"]?.jsonArray
            ?: error("VCAP_SERVICES present, but no 'xsuaa' entry found")

        val first = xsuaa
            .map { it.jsonObject }
            .firstOrNull() ?: error("Invalid xsuaa binding structure.")
        log.info("XSUAA binding: $first")

        val credentials = first["credentials"]?.jsonObject
            ?: error("Invalid xsuaa binding structure.")

        log.info("XSUAA credentials: $credentials")
    //    log.info("XSUAA url: ${credentials["url"]}")
        log.info("url type=${credentials["url"]?.let { it::class.qualifiedName }}")
        log.info("XSUAA xsappname: ${credentials["xsappname"]}")
        log.info("XSUAA clientid: ${credentials["clientid"]}")
        log.info("XSUAA identityzone: ${credentials["identityzone"]}")
        log.info("XSUAA identityzoneid: ${credentials["identityzoneid"]}")

        fun s(key: String) = credentials[key]?.jsonPrimitive?.content ?: error("Missing xsuaa credential: $key")


        return XsuaaCredentials(
            url = s("url"),
            xsappname = s("xsappname"),
            clientid = s("clientid"),
            identityzone = credentials["identityzone"] as? String,
            identityzoneid = credentials["identityzoneid"] as? String
        )
    } else {
        error("VCAP_SERVICES is empty")
    }
}
