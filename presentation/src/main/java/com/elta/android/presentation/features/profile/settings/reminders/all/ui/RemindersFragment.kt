package com.elta.android.presentation.features.profile.settings.reminders.all.ui

import android.Manifest
import android.app.AlarmManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.features.profile.settings.reminders.all.model.RemindersAction
import com.elta.android.presentation.features.profile.settings.reminders.all.model.RemindersEvent
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.widgets.ReminderScreen
import com.elta.android.presentation.features.profile.settings.reminders.all.viewmodels.RemindersViewModel
import com.elta.android.presentation.notifications.areAlarmsAndRemindersEnabled
import com.elta.android.presentation.notifications.areNotificationsEnabled
import com.elta.android.presentation.utils.openAlarmsAndRemindersSettingsIntent
import com.elta.android.presentation.utils.openNotificationSettingsIntent

class RemindersFragment : BaseComposeFragment<RemindersViewModel>() {

    override val viewModel: RemindersViewModel by viewModels { viewModelFactory }

    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var alarmManager: AlarmManager

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            viewModel.sendAction(RemindersAction.NotificationPermissionResult(isGranted))
        }

    override fun RemindersViewModel.init() {
        notificationManager = NotificationManagerCompat.from(requireContext())
        alarmManager = requireContext().getSystemService(AlarmManager::class.java)

        appTopBar.setStartIconAction(AppAction.BackPressure)
        appTopBar.setEndIconAction(RemindersAction.CreateReminder)

        notificationSettingsDialog.initDialog(
            title = getString(R.string.settings_dialog_settings_reminder),
            message = getString(R.string.notification_dialog_message),
            positiveButtonText = getString(R.string.settings_dialog_positive),
            negativeButtonText = getString(R.string.settings_dialog_negative)
        )
        alarmsAndRemindersSettingsDialog.initDialog(
            title = getString(R.string.settings_dialog_settings_reminder),
            message = getString(R.string.alarms_and_reminders_dialog_message),
            positiveButtonText = getString(R.string.settings_dialog_positive),
            negativeButtonText = getString(R.string.settings_dialog_negative)
        )
    }

    @Composable
    override fun Content(viewModel: RemindersViewModel) {

        ReminderScreen(viewModel)

        val event = viewModel.event.collectAsState(initial = null).value
        LaunchedEffect(key1 = event) {
            when (event) {
                is RemindersEvent.CheckNotificationPermission -> checkNotificationPermission()
                is RemindersEvent.OpenNotificationSettings -> openNotificationSettingsIntent(requireContext())
                is RemindersEvent.OpenAlarmAndRemindersSettings -> openAlarmsAndRemindersSettingsIntent(requireContext())
            }
        }

    }

    @Composable
    override fun Dialogs(viewModel: RemindersViewModel) {
        BaseDialog(widgetModel = viewModel.notificationSettingsDialog)
        BaseDialog(widgetModel = viewModel.alarmsAndRemindersSettingsDialog)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            resultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.sendAction(RemindersAction.OpenNotificationSettingsDialog)
        }
    }

    private fun checkNotificationPermission() {
        val notificationsIsEnabled = areNotificationsEnabled(notificationManager)
        val alarmsAndRemindersIsEnabled = areAlarmsAndRemindersEnabled(alarmManager)
        when {
            !notificationsIsEnabled -> requestNotificationPermission()
            !alarmsAndRemindersIsEnabled -> viewModel.sendAction(RemindersAction.OpenAlarmsAndRemindersSettingsDialog)
            else -> viewModel.sendAction(RemindersAction.OpenCreateReminder)
        }
    }
}
