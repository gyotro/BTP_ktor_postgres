package com.postgres.security

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.respond
import java.net.URL
import java.util.concurrent.TimeUnit

//import org.slf4j.event.*

fun Application.configureXsuaaAuthentication() {
    val log = environment.log
    val xsuaa = readXsuaaCredentialsFromVcap()
    log.info("XSUAA credentials: $xsuaa")
    val expectedIssuer = "${xsuaa.url}/oauth/token"
    val tokenKeysUrl = URL("${xsuaa.url}/token_keys")
    val jwkProvider = JwkProviderBuilder(tokenKeysUrl)
        .cached(10, 24, TimeUnit.HOURS)       // cache keys for rotation
        .rateLimited(10, 1, TimeUnit.MINUTES) // avoid hammering
        .build()
    val jwtRealm = "ktor-api"

    install(Authentication) {
        jwt("xsuaa") {
            realm = jwtRealm
            verifier(jwkProvider, expectedIssuer) {
                withIssuer(expectedIssuer)
                withAudience(xsuaa.clientid)
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


