package com.elta.android.presentation.features.profile.settings.reminders.all.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import com.nullgr.core.intents.launch

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

        settingDialog.initDialog(
            title = getString(R.string.notifications_dialog_title),
            message = getString(R.string.notifications_dialog_message),
            positiveButtonText = getString(R.string.notifications_dialog_positive),
            negativeButtonText = getString(R.string.notifications_dialog_negative)
        )
    }

    @Composable
    override fun Content(viewModel: RemindersViewModel) {

        ReminderScreen(viewModel)

        val event = viewModel.event.collectAsState(initial = null).value
        LaunchedEffect(key1 = event) {
            when (event) {
                is RemindersEvent.CheckNotificationPermission -> checkNotificationPermission()
                is RemindersEvent.OpenSettings -> openNotificationsSettings()
            }
        }

    }

    @Composable
    override fun Dialogs(viewModel: RemindersViewModel) {
        BaseDialog(widgetModel = viewModel.settingDialog)
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


    private fun openNotificationsSettings() {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            .launch(requireContext())
    }
}
