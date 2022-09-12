package com.elta.android.presentation.utils.dynamic_links

import android.net.Uri

const val NOTIFICATION_URI_SCHEME = "app"
const val NOTIFICATION_URI_AUTHORITY = "com.elta.android.notification"
const val HOME_SCREEN = "home"

fun Uri?.isNotificationUriValid(): Boolean {
    return this?.let {
        return scheme == NOTIFICATION_URI_SCHEME &&
            authority == NOTIFICATION_URI_AUTHORITY
    } ?: false
}
