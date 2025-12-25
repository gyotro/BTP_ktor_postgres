package com.postgres.util

import com.postgres.db.ExposedUser
import com.postgres.dto.request.request.UserDTO
import com.postgres.model.UserModel
import java.util.UUID

fun UserDTO.mapToUserModel(): UserModel {
    return UserModel(
        id = this.id,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email
    )
}

fun UserModel.mapToUserDTO(): UserDTO {
    return UserDTO(
        id = this.id,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email
    )
}

fun ExposedUser.mapToUserModel(): UserModel{
    return UserModel(
        id = UUID.fromString(this.id),
        firstName = this.firstname,
        lastName = this.lastname,
        email = this.email
    )
}