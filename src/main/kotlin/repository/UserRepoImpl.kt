package com.postgres.repository

import com.postgres.db.ExposedUser
import com.postgres.db.UserServiceDB
import com.postgres.model.UserModel
import com.postgres.util.mapToUserModel
import java.util.UUID

class UserRepoImpl(private val userService: UserServiceDB): UserRepo {
    override suspend fun getAll(): List<UserModel> {
        return userService.readAll().map {
            it.mapToUserModel()
        }
    }

    override suspend fun getById(id: UUID): UserModel? {
        return userService.read(id)?.mapToUserModel()
    }

    override suspend fun create(user: UserModel): UUID {
        return userService.create(ExposedUser(firstname = user.firstName, lastname = user.lastName, email = user.email))
    }

    override suspend fun delete(id: UUID): Boolean {
        return userService.delete(id)
    }

    override suspend fun update(user: UserModel): Boolean {
       return userService.update(user.id, ExposedUser(firstname = user.firstName, lastname = user.lastName, email = user.email))
    }

    override suspend fun userExists(id: UUID): Boolean {
        return this.getById(id) != null
    }

}