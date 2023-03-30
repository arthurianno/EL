package com.elta.android.common.logger.model

import android.os.Build
import com.elta.android.common.BuildConfig

data class DeviceDetails(
    val deviceId: String,
    val osVersion: String = Build.VERSION.RELEASE,
    val manufacturer: String = Build.MANUFACTURER,
    val brand: String = Build.BRAND,
    val device: String = Build.DEVICE,
    val model: String = Build.MODEL,
    val appVersion: String = BuildConfig.APP_VERSION
)
