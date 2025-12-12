package com.elta.android.domain.features.multiLangsConfig.model

sealed class ApiError {
    data class ValidationError(val field: String, val message: String) : ApiError()
    object ResourceNotFound : ApiError()

}