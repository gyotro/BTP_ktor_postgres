package com.postgres.errors

sealed interface XsuaaConfigError {
    data class MissingEnv(val name: String) : XsuaaConfigError
    data class InvalidStructure(val message: String) : XsuaaConfigError
    data class MissingCredential(val key: String) : XsuaaConfigError
}