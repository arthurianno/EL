package com.elta.android.domain.features.googlefit.model

sealed class GoogleFitAuthResult {
    object Access : GoogleFitAuthResult()
    object NotAccess: GoogleFitAuthResult()
    object ApplicationNotInstalled : GoogleFitAuthResult()
}