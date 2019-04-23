package com.elta.android.presentation.core.notification

interface NotificationSource {

    fun sendNotification(title: String, text: String, id: String)
}