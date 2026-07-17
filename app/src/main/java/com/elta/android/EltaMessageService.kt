package com.elta.android

import android.content.Context
import com.elta.android.common.utils.EltaMessageClient
import com.elta.android.presentation.utils.LocaleHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import javax.inject.Inject

class EltaMessageService : FirebaseMessagingService() {

    @Inject
    lateinit var messageClient: EltaMessageClient

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        LocaleHelper.onAttach(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // No-op: Webim notifications are removed. Other notifications are handled by OneSignal.
    }
}
