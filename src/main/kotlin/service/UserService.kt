package com.postgres.service

import com.postgres.dto.request.request.UserDTO
import com.postgres.dto.request.respose.ApiResponse
import com.postgres.repository.UserRepo
import com.postgres.util.mapToUserDTO
import com.postgres.util.mapToUserModel
import java.util.UUID

class UserService(private val userRepo: UserRepo) {
    suspend fun getAllUsers(): ApiResponse<List<UserDTO>> {
        val listUsers = userRepo.getAll()
            .map { it.mapToUserDTO() }
        return ApiResponse(data = listUsers)
    }

    suspend fun getUserById(id: String): ApiResponse<UserDTO> {
        val user = userRepo.getById(UUID.fromString(id))?.mapToUserDTO()
        return ApiResponse(data = user)
    }

    suspend fun createUser(user: UserDTO): ApiResponse<UserDTO> {
        val user = userRepo.create(user.mapToUserModel())
        return if (user) {
            ApiResponse()
        } else
            ApiResponse(code = 500, message = "User not created")
    }

    suspend fun updateUser(user: UserDTO): ApiResponse<UserDTO> {
        val user = userRepo.upsert(user.mapToUserModel())
        return if (user) {
            ApiResponse()
        } else
            ApiResponse(code = 500, message = "User not created")
    }

    suspend fun deleteUser(id: String): ApiResponse<UserDTO> {
        val user = userRepo.delete(UUID.fromString(id))
        return if (user) {
            ApiResponse()
        } else
            ApiResponse(code = 500, message = "User not created")
    }
}