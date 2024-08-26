package com.elta.android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.elta.android.common.utils.EltaMessageClient
import com.elta.android.presentation.features.app.ui.AppActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import ru.webim.android.sdk.Webim
import ru.webim.android.sdk.WebimPushNotification
import javax.inject.Inject

private const val CHANNEL_ID = "ELTA_MAIN_CHANNEL"
private const val NOTIFICATION_ID = 37
private const val OPERATOR_EVENT_NAME = "add"

class EltaMessageService : FirebaseMessagingService() {

    @Inject
    lateinit var messageClient: EltaMessageClient

    private val notificationManager: NotificationManager by lazy {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        getString(com.elta.android.presentation.R.string.push_channel_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
        }
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val webimPush = Webim.parseFcmPushNotification(message.data.toString())
        val isWebimPush = webimPush != null && webimPush.event == OPERATOR_EVENT_NAME

        val (title, body) = if (isWebimPush) {
            val body = getWebimMessageType(webimPush?.type)
            val title = resources.getString(com.elta.android.presentation.R.string.app_name)
            title to body
        } else message.notification?.title to message.notification?.body

        val icon = if (isWebimPush) com.elta.android.presentation.R.drawable.ic_app_logo
        else com.elta.android.presentation.R.drawable.ic_notification_reminder

        val notification = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle(title)
            setContentText(body)
            setSmallIcon(icon)
            setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText(body)
            )
            if (isWebimPush) {
                val consultantIntent =
                    Intent(this@EltaMessageService, AppActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra(AppActivity.OPEN_CONSULTANT_CHAT, true)
                    }

                val pendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    NOTIFICATION_ID,
                    consultantIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                setContentIntent(pendingIntent)
            }
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_HIGH
        }.build()
        if (isWebimPush && messageClient.isConsultantScreenActive) {
            notificationManager.cancel(NOTIFICATION_ID)
        } else {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun getWebimMessageType(type: WebimPushNotification.NotificationType?): String? {
        val webimType = when (type) {
            WebimPushNotification.NotificationType.OPERATOR_ACCEPTED ->
                resources.getString(com.elta.android.presentation.R.string.consultant_push_operator_joined)

            WebimPushNotification.NotificationType.OPERATOR_FILE ->
                resources.getString(com.elta.android.presentation.R.string.consultant_push_file_message)

            WebimPushNotification.NotificationType.OPERATOR_MESSAGE ->
                resources.getString(com.elta.android.presentation.R.string.consultant_push_text_message)

            WebimPushNotification.NotificationType.RATE_OPERATOR ->
                resources.getString(com.elta.android.presentation.R.string.consultant_rate_operator)

            else -> null
        }
        return webimType
    }
}
