package com.elta.android.domain.features.multiLangsConfig.model

sealed class Resource<out T> {
    class Loading<out T> : Resource<T>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error<out T>(val message: String, val errorType: ErrorType) : Resource<T>()
}

enum class ErrorType {
    NETWORK, VALIDATION, NOT_FOUND, UNKNOWN, EMPY_RESPONSE
}
