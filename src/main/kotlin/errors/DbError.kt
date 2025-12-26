package com.postgres.errors

sealed interface DbError {
    data class Connection(val cause: Throwable) : DbError
    data class Constraint(val name: String?, val cause: Throwable) : DbError
    data class Unexpected(val cause: Throwable) : DbError
}
