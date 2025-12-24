package com.postgres.dto.request.request

import com.postgres.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserDTO(
    @Serializable(with = UUIDSerializer::class)
    var id: UUID? = null,
    val firstName: String,
    val lastName: String,
    val email: String
)