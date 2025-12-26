package com.postgres.repository

import arrow.core.Either
import arrow.core.raise.*
import com.postgres.db.ExposedUser
import com.postgres.db.UserServiceDB
import com.postgres.errors.UserDbError
import com.postgres.errors.UserRepoError
import com.postgres.model.UserModel
import com.postgres.util.mapToUserModel
import java.util.UUID

class UserRepoImpl(private val userService: UserServiceDB) : UserRepo {

    override suspend fun getAll(): Either<UserRepoError, List<UserModel>> =
        userService.readAll()
            .mapLeft { UserRepoError.Db(it) }
            .map { list -> list.map { it.mapToUserModel() } }

    override suspend fun getById(id: UUID): Either<UserRepoError, UserModel?> =
        userService.read(id)
            .mapLeft { UserRepoError.Db(it) }
            .map { it?.mapToUserModel() }

    override suspend fun create(user: UserModel): Either<UserRepoError, UUID> =
        userService.create(
            ExposedUser(
                firstname = user.firstName,
                lastname = user.lastName,
                email = user.email
            )
        )
            .mapLeft { it.toRepoErrorOnWrite() }

    override suspend fun delete(id: UUID): Either<UserRepoError, Unit> =
        either {
            val affected = userService.delete(id)
                .mapLeft { it.toRepoErrorOnWrite() }
                .bind()

            ensure(affected > 0) { UserRepoError.NotFound(id) }
            Unit
        }

    override suspend fun update(user: UserModel): Either<UserRepoError, Unit> =
        either {
            val id = user.id
            // If your UserModel.id is nullable, handle it here with ensureNotNull()
            val affected = userService.update(
                id,
                ExposedUser(
                    firstname = user.firstName,
                    lastname = user.lastName,
                    email = user.email
                )
            ).mapLeft { it.toRepoErrorOnWrite() }
                .bind()

            ensure(affected > 0) { UserRepoError.NotFound(id) }
            Unit
        }

    override suspend fun userExists(id: UUID): Either<UserRepoError, Boolean> =
        getById(id).map { it != null }

    private fun UserDbError.toRepoErrorOnWrite(): UserRepoError =
        when (this) {
            is UserDbError.UniqueViolation ->
                UserRepoError.Conflict("User already exists (unique constraint on ${field ?: "field"})")
            is UserDbError.Unexpected ->
                UserRepoError.Db(this)
        }
}