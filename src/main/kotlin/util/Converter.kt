package com.postgres.util

import com.postgres.dto.request.request.UserDTO
import com.postgres.model.UserModel

fun UserDTO.mapToUserModel(): UserModel {
    return UserModel(
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
