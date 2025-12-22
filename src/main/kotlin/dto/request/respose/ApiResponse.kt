package com.postgres.dto.request.respose

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val code: Int = HttpStatusCode.OK.value,
    val message: String = "Success",
    val data: T? = null
)
