package com.elta.android.presentation.features.googlefit

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.elta.android.common.constants.GOOGLE_FIT_PACKAGE_NAME
import com.elta.android.common.constants.PLAY_MARKET_URI
import com.nullgr.core.intents.launch

fun Context.openGoogleFitInStoreIntent() {
    val uri = Uri.parse(PLAY_MARKET_URI + GOOGLE_FIT_PACKAGE_NAME)
    Intent(Intent.ACTION_VIEW, uri)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .launch(this)
}

fun Context.openGoogleFitIntent() {
    Intent(this, RxGoogleFitAuthActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .launch(this)
}
