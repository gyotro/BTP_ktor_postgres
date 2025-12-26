package com.postgres.security

import com.postgres.errors.XsuaaConfigError
import kotlinx.serialization.json.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import arrow.core.*
import arrow.core.raise.either

data class XsuaaCredentials(
    val url: String,
    val xsappname: String,
    val clientid: String,
    val identityzone: String? = null,
    val identityzoneid: String? = null
)

fun readXsuaaCredentialsFromVcap(): Either<XsuaaConfigError, XsuaaCredentials> =
    Either.catch {
        System.getenv("VCAP_SERVICES")
    }.mapLeft {
        XsuaaConfigError.MissingEnv("VCAP_SERVICES")
    }.flatMap { vcap ->
        if (vcap.isBlank())
            XsuaaConfigError.MissingEnv("VCAP_SERVICES").left()
        else
            parseXsuaaFromVcap(vcap)
    }

private fun parseXsuaaFromVcap(vcap: String): Either<XsuaaConfigError, XsuaaCredentials> =
    either {
        val log: Logger = LoggerFactory.getLogger("SecurityConfig")

        val root = Json.parseToJsonElement(vcap).jsonObject

        val xsuaaArray = root["xsuaa"]?.jsonArray
            ?: raise(XsuaaConfigError.InvalidStructure("No 'xsuaa' entry found"))

        val binding = xsuaaArray
            .firstOrNull()
            ?.jsonObject
            ?: raise(XsuaaConfigError.InvalidStructure("Empty xsuaa array"))

        log.info("XSUAA binding found")

        val credentials = binding["credentials"]?.jsonObject
            ?: raise(XsuaaConfigError.InvalidStructure("Missing credentials object"))

        fun s(key: String): String =
            credentials[key]?.jsonPrimitive?.content
                ?: raise(XsuaaConfigError.MissingCredential(key))

        XsuaaCredentials(
            url = s("url"),
            xsappname = s("xsappname"),
            clientid = s("clientid"),
            identityzone = credentials["identityzone"]?.jsonPrimitive?.content,
            identityzoneid = credentials["identityzoneid"]?.jsonPrimitive?.content
        )
    }

