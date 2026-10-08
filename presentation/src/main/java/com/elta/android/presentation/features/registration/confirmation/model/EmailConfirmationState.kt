package com.elta.android.presentation.features.registration.confirmation.model

data class EmailConfirmationState(
    val status: ConfirmationStatus = ConfirmationStatus.Loading
)

sealed interface ConfirmationStatus {
    object Loading : ConfirmationStatus
    object OtherAccount : ConfirmationStatus
    data class Failure(val message: String?, val isServerUnavailable: Boolean) : ConfirmationStatus
}
