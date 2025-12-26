package com.postgres.errors

import java.util.UUID

sealed interface UserError {
    data class InvalidId(val raw: String) : UserError
    data class NotFound(val id: UUID?) : UserError
    data class Conflict(val message: String) : UserError
    data class Internal(val message: String, val cause: Throwable? = null) : UserError
}
