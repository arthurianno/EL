package com.elta.android.data.features.auth.datasource.social.delegates

import android.app.Activity
import android.content.Intent
import android.os.Bundle

abstract class ActivityDelegate(val activity: Activity) {

    abstract fun onCreate(state: Bundle?)

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean = false

    open fun onBackPressed() {
    }
}