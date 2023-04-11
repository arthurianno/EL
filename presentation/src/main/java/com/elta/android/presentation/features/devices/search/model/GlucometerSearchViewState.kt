package com.elta.android.presentation.features.devices.search.model

data class GlucometerSearchViewState(
    val searchStatus: GlucometerSearchStatus,
    val glucometerAddress: String,
    val snackBar: SnackBarText
)
