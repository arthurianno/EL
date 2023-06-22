package com.elta.android.presentation.features.profile.settings.reminders.all.ui

import android.Manifest
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
import com.elta.android.presentation.notifications.areNotificationsEnabled
import com.elta.android.presentation.utils.openNotificationSettingsIntent

class RemindersFragment : BaseComposeFragment<RemindersViewModel>() {

    override val viewModel: RemindersViewModel by viewModels { viewModelFactory }

    private lateinit var notificationManager: NotificationManagerCompat

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            viewModel.sendAction(RemindersAction.PermissionResult(isGranted))
        }

    override fun RemindersViewModel.init() {
        notificationManager = NotificationManagerCompat.from(requireContext())

        appTopBar.setStartIconAction(AppAction.BackPressure)
        appTopBar.setEndIconAction(RemindersAction.CreateReminder)

        settingsDialog.initDialog(
            title = getString(R.string.settings_dialog_title),
            message = getString(R.string.notification_dialog_message),
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
                is RemindersEvent.OpenSettings -> openNotificationSettingsIntent(requireContext())
            }
        }

    }

    @Composable
    override fun Dialogs(viewModel: RemindersViewModel) {
        BaseDialog(widgetModel = viewModel.settingsDialog)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            resultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.sendAction(RemindersAction.OpenSettingsDialog)
        }
    }

    private fun checkNotificationPermission() {
        if (areNotificationsEnabled(notificationManager)) {
            viewModel.sendAction(RemindersAction.OpenCreateReminder)
        } else {
            requestNotificationPermission()
        }
    }
}
