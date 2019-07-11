package com.elta.android.presentation.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
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

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, title,
                NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        } else {
            notification
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setLights(Color.BLUE, LIGHTS_DURATION, LIGHTS_DURATION)
        }

        manager.notify(id.hashCode(), notification.build())
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "ELTA Notification"
        private const val LIGHTS_DURATION = 5000
    }
}