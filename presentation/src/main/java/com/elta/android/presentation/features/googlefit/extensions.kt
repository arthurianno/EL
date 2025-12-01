package com.elta.android.presentation.features.googlefit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.elta.android.common.constants.GOOGLE_FIT_PACKAGE_NAME
import com.elta.android.common.constants.PLAY_MARKET_URI
import com.nullgr.core.intents.launch

fun Context.openGoogleFitInStoreIntent() {
    val uri = Uri.parse(PLAY_MARKET_URI + GOOGLE_FIT_PACKAGE_NAME)
    Intent(Intent.ACTION_VIEW, uri)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .launch(this)
}

/**
 * Opens health app authorization:
 * - Android 14+ (API 34+): Health Connect permissions
 * - Android 13 and below: Google Fit permissions
 */
fun Context.openGoogleFitIntent() {
    val activityClass = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        // Android 14+ - use Health Connect
        HealthConnectAuthActivity::class.java
    } else {
        // Android 13 and below - use Google Fit
        RxGoogleFitAuthActivity::class.java
    }

    Intent(this, activityClass)
        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .launch(this)
}
