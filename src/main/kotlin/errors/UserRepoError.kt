package com.postgres.errors

import java.util.UUID

sealed interface UserRepoError {
    data class NotFound(val id: UUID?) : UserRepoError
    data class Conflict(val message: String) : UserRepoError
    data class Db(val cause: UserDbError) : UserRepoError
}
