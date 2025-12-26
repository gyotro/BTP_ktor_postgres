package com.postgres.errors

sealed interface UserDbError {
    data class Unexpected(val cause: Throwable) : UserDbError
    data class UniqueViolation(val field: String?, val cause: Throwable) : UserDbError
}
