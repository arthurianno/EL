package com.elta.android.presentation.features.devices.search.model

import androidx.annotation.StringRes
import com.elta.android.presentation.R

enum class SnackBarText(@StringRes val stringId: Int) {
    Connecting(stringId = R.string.profile_device_snackbar_connecting_to_device)
}
