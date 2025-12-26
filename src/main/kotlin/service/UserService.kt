package com.postgres.service

import com.postgres.dto.request.request.UserDTO
import com.postgres.errors.UserError
import com.postgres.errors.UserRepoError
import com.postgres.repository.UserRepo
import com.postgres.util.mapToUserDTO
import com.postgres.util.mapToUserModel
import java.util.UUID

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull

class UserService(private val userRepo: UserRepo) {

    suspend fun getAllUsers(): Either<UserError, List<UserDTO>> =
        either {
            val users = userRepo.getAll()
                .mapLeft { it.toUserError() }
                .bind()

            users.map { it.mapToUserDTO() }
        }

    suspend fun getUserById(id: String): Either<UserError, UserDTO> =
        either {
            val uuid = parseUuid(id).bind()

            val user = userRepo.getById(uuid)
                .mapLeft { it.toUserError() }
                .bind()

            ensureNotNull(user) { UserError.NotFound(uuid) }
            user.mapToUserDTO()
        }

    suspend fun createUser(dto: UserDTO): Either<UserError, UUID> =
        userRepo.create(dto.mapToUserModel())
            .mapLeft { it.toUserError() }

    suspend fun updateUser(dto: UserDTO): Either<UserError, Unit> =
        userRepo.update(dto.mapToUserModel())
            .mapLeft { it.toUserError() }

    suspend fun deleteUser(id: String): Either<UserError, Unit> =
        either {
            val uuid = parseUuid(id).bind()
            userRepo.delete(uuid)
                .mapLeft { it.toUserError() }
                .bind()
        }

    private fun parseUuid(raw: String): Either<UserError, UUID> =
        Either.catch { UUID.fromString(raw) }
            .mapLeft { UserError.InvalidId(raw) }

    private fun UserRepoError.toUserError(): UserError =
        when (this) {
            is UserRepoError.NotFound -> UserError.NotFound(this.id)
            is UserRepoError.Conflict -> UserError.Conflict(this.message)
            is UserRepoError.Db -> UserError.Internal("Database error", cause = null)
        }
}
