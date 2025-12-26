package com.postgres.dto.request.respose
import com.postgres.errors.UserError
import arrow.core.Either
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
sealed interface ApiResult<out T> {
    @Serializable
    data class Ok<T>(val data: T, val message: String = "Success") : ApiResult<T>

    @Serializable
    data class Error(
        val message: String,
        val errorCode: String,
        val details: Map<String, String> = emptyMap()
    ) : ApiResult<Nothing>
}

suspend inline fun <reified T> ApplicationCall.respondEither(result: Either<UserError, T>, httpStatus: HttpStatusCode = HttpStatusCode.OK) {
    result.fold(
        { err -> respondUserError(err) },
        { data -> respond(status = httpStatus, message = ApiResult.Ok(data)) }
    )
}

suspend fun ApplicationCall.respondUserError(err: UserError) {
    when (err) {
        is UserError.InvalidId ->
            respond(HttpStatusCode.BadRequest,
                ApiResult.Error(
                    message = "Invalid id",
                    errorCode = "INVALID_ID",
                    details = mapOf("id" to err.raw)
                )
            )

        is UserError.NotFound ->
            respond(HttpStatusCode.NotFound,
                ApiResult.Error(
                    message = "User not found",
                    errorCode = "NOT_FOUND",
                    details = mapOf("id" to err.id.toString())
                )
            )

        is UserError.Conflict ->
            respond(HttpStatusCode.Conflict,
                ApiResult.Error(
                    message = err.message,
                    errorCode = "CONFLICT"
                )
            )

        is UserError.Internal ->
            respond(HttpStatusCode.InternalServerError,
                ApiResult.Error(
                    message = err.message,
                    errorCode = "INTERNAL_ERROR"
                )
            )
    }
}
