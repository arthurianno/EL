package com.elta.android.presentation.features.version

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.elta.android.common.constants.PLAY_MARKET_URI
import com.nullgr.core.intents.launch

fun Context.openAppInStoreIntent(packageName: String) {
    val uri = Uri.parse(PLAY_MARKET_URI + packageName)
    Intent(Intent.ACTION_VIEW, uri)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .launch(this)
}
