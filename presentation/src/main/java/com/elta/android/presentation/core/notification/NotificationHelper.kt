package com.elta.android.presentation.core.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.support.v4.app.NotificationCompat
import com.elta.android.presentation.R
import com.elta.android.presentation.features.app.ui.AppActivity
import com.elta.android.presentation.utils.dynamic_links.NOTIFICATION_URI_AUTHORITY
import com.elta.android.presentation.utils.dynamic_links.NOTIFICATION_URI_SCHEME
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    private val context: Context
) : NotificationSource {

    private val manager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun sendNotification(screen: String, title: String, text: String, id: String) {
        val intent = Intent(context, AppActivity::class.java).apply {
            data = Uri.Builder()
                .scheme(NOTIFICATION_URI_SCHEME)
                .authority(NOTIFICATION_URI_AUTHORITY)
                .appendPath(screen)
                .build()
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, title, NotificationManager.IMPORTANCE_HIGH)
            channel.lightColor = Color.BLUE
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)

        manager.notify(id.hashCode(), notification.build())
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "ELTA Notification"
    }
}