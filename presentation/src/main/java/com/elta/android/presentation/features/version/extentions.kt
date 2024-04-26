package com.elta.android.presentation.features.version

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.elta.android.common.constants.HUAWEI_MARKET_APP_URL
import com.elta.android.common.constants.PLAY_MARKET_URI
import com.elta.android.presentation.BuildConfig
import com.nullgr.core.intents.launch

fun Context.openAppInStoreIntent(packageName: String) {
    val url = when(BuildConfig.APP_STORE) {
        "huawei" -> HUAWEI_MARKET_APP_URL
        else -> PLAY_MARKET_URI + packageName
    }
    val uri = Uri.parse(url)
    Intent(Intent.ACTION_VIEW, uri)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .launch(this)
}
