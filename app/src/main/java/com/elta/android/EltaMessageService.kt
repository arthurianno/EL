package com.elta.android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

private const val TITLE = "title"
private const val BODY = "body"
private const val DATA = "data"
private const val CHANNEL_ID = "ELTA_MAIN_CHANNEL"
private const val NOTIFICATION_ID = 37

class EltaMessageService : FirebaseMessagingService() {

    private val notificationManager: NotificationManager by lazy {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        getString(R.string.push_channel_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.data[TITLE]
        val body = message.data[BODY]
        val notification = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle(title)
            setContentText(body)
            setSmallIcon(R.drawable.ic_firmware_logo)
            setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText(body)
            )
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_DEFAULT
        }.build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
