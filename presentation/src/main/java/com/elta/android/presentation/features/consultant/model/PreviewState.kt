package com.elta.android.presentation.features.consultant.model

import android.net.Uri

data class PreviewState(
    val isPhotoPreview: Boolean,
    val isFromCamera: Boolean,
    val uriPhoto: Uri?,
    val urlPhoto: String?
)
