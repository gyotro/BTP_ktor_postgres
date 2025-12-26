package com.postgres.security

import arrow.core.Either
import arrow.core.getOrElse
import com.auth0.jwk.JwkProviderBuilder
import com.postgres.errors.XsuaaConfigError
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.respond
import java.net.URL
import java.util.concurrent.TimeUnit

fun Application.configureXsuaaAuthentication() {
    val log = environment.log

    // readXsuaaCredentialsFromVcap(): Either<XsuaaConfigError, XsuaaCredentials>
    val xsuaa = readXsuaaCredentialsFromVcap().getOrElse { err ->
        // Fail fast: without XSUAA config, the service should not start secured.
        val msg = when (err) {
            is XsuaaConfigError.MissingEnv ->
                "XSUAA config error: missing env var '${err.name}'"
            is XsuaaConfigError.InvalidStructure ->
                "XSUAA config error: invalid VCAP_SERVICES structure: ${err.message}"
            is XsuaaConfigError.MissingCredential ->
                "XSUAA config error: missing credential '${err.key}' in VCAP_SERVICES xsuaa binding"
        }
        log.error(msg)
        throw IllegalStateException(msg)
    }

    log.info("XSUAA credentials loaded. xsappname=${xsuaa.xsappname}, clientid=${xsuaa.clientid}")

    val baseUrl = xsuaa.url.trimEnd('/')
    val expectedIssuer = "$baseUrl/oauth/token"
    val tokenKeysUrl = URL("$baseUrl/token_keys")

    val jwkProvider = JwkProviderBuilder(tokenKeysUrl)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt("xsuaa") {
            realm = "ktor-api"

            verifier(jwkProvider, expectedIssuer) {
                withIssuer(expectedIssuer)
                withAudience(xsuaa.clientid) // your token aud includes clientid
                acceptLeeway(5)              // small clock skew tolerance
            }

            validate { credential ->
                JWTPrincipal(credential.payload)
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Invalid or missing JWT")
            }
        }
    }
}
