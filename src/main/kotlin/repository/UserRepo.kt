package com.postgres.repository

import com.postgres.model.UserModel
import java.util.UUID

interface UserRepo {

    suspend fun getAll(): List<UserModel>

    suspend fun getById(id: UUID): UserModel?

    suspend fun create(user: UserModel): Boolean

    suspend fun delete(id: UUID): Boolean

    suspend fun upsert(user: UserModel): Boolean

    suspend fun userExists(id: UUID): Boolean
}