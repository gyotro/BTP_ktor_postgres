package com.postgres.repository

import arrow.core.Either
import com.postgres.errors.UserRepoError
import com.postgres.model.UserModel
import java.util.UUID

interface UserRepo {
    suspend fun getAll(): Either<UserRepoError, List<UserModel>>
    suspend fun getById(id: UUID): Either<UserRepoError, UserModel?>
    suspend fun create(user: UserModel): Either<UserRepoError, UUID>
    suspend fun delete(id: UUID): Either<UserRepoError, Unit>
    suspend fun update(user: UserModel): Either<UserRepoError, Unit>
    suspend fun userExists(id: UUID): Either<UserRepoError, Boolean>
}