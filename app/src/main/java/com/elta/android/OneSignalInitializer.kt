import android.content.Context
import android.content.Intent
import android.util.Log
import com.elta.android.BuildConfig
import com.elta.android.presentation.features.app.ui.AppActivity
import com.onesignal.OneSignal
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationClickListener
import com.onesignal.notifications.INotificationLifecycleListener
import com.onesignal.notifications.INotificationWillDisplayEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

object OneSignalInitializer {

    private const val ONESIGNAL_APP_ID = "97cdb893-9c04-4eaa-90e4-28dc9a938754"

    fun initialize(context: Context) {
        // Инициализация OneSignal в версии 5.x
        OneSignal.initWithContext(context, ONESIGNAL_APP_ID)

        CoroutineScope(Dispatchers.Main).launch {
            val accepted = OneSignal.Notifications.requestPermission(true)
           Log.e("OneSignal", "Permission accepted: $accepted")
        }

        OneSignal.User.addTag("environment", BuildConfig.ENVIRONMENT_TAG)

        // Обработчик для показа уведомлений в foreground (без изменений)
        OneSignal.Notifications.addForegroundLifecycleListener(object : INotificationLifecycleListener {
            override fun onWillDisplay(event: INotificationWillDisplayEvent) {
                Timber.d("OneSignal: Notification will display in foreground")
                event.notification.display()
            }
        })

        // ОБРАБОТЧИК КЛИКОВ ПО УВЕДОМЛЕНИЯМ (ЕДИНЫЙ ДЛЯ FOREGROUND И BACKGROUND В 5.X)
        OneSignal.Notifications.addClickListener(object : INotificationClickListener {
            override fun onClick(event: INotificationClickEvent) {
                Timber.d("OneSignal: Notification clicked! Data: ${event.notification.additionalData}")

                val data = event.notification.additionalData
                var launchUrl: String? = null
                try {
                    if (data != null && data.has("launchURL")) {
                        launchUrl = data.getString("launchURL")
                        Timber.d("OneSignal: Parsed launchURL = $launchUrl")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Ошибка парсинга launchURL из additionalData")
                }

                // Создаем Intent для AppActivity
                val intent = Intent(context, AppActivity::class.java).apply {
                    if (launchUrl != null) {
                        putExtra("launch_url", launchUrl)
                        Timber.d("OneSignal: Setting extra launch_url = $launchUrl")
                    }
                    putExtra("push_data", data?.toString() ?: "")
                    // Флаги для запуска из фона/закрытого состояния
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }

                // Запускаем активити
                context.startActivity(intent)

                Timber.d("OneSignal: Started AppActivity with launchURL = $launchUrl")
            }
        })
    }
}