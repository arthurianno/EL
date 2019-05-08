package com.elta.android.presentation.core.notification

import com.elta.android.presentation.utils.dynamic_links.HOME_SCREEN

interface NotificationSource {

    fun sendNotification(
        screen: String = HOME_SCREEN,
        title: String,
        text: String,
        id: String
    )
}