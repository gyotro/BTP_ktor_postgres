package com.postgres.errors

sealed interface DbError {
    data class Connection(val cause: Throwable) : DbError
    data class Constraint(val name: String?, val cause: Throwable) : DbError
    data class Unexpected(val cause: Throwable) : DbError
    data class MissingEnv(val name: String) : DbError
    data class InvalidVcap(val message: String, val cause: Throwable? = null) : DbError
    data class MissingVcapEntry(val serviceLabel: String) : DbError
    data class MissingCredential(val key: String) : DbError
    data class BadCredential(val key: String, val value: String) : DbError
    data class Io(val message: String, val cause: Throwable) : DbError
    data class Hikari(val message: String, val cause: Throwable) : DbError
}
