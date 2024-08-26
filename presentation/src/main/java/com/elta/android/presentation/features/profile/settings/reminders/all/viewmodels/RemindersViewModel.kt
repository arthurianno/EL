package com.elta.android.presentation.features.profile.settings.reminders.all.viewmodels

import com.elta.android.domain.features.reminder.interactor.GetRemindersUseCase
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.features.profile.settings.reminders.all.mapper.ReminderMapper
import com.elta.android.presentation.features.profile.settings.reminders.all.model.RemindersAction
import com.elta.android.presentation.features.profile.settings.reminders.all.model.RemindersEvent
import com.elta.android.presentation.features.profile.settings.reminders.all.model.RemindersViewState
import com.nullgr.core.rx.RxBus
import io.reactivex.Observable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.rx2.asFlow
import javax.inject.Inject

class RemindersViewModel @Inject constructor(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val mapper: ReminderMapper,
    private val appMetricTracker: AppMetricTracker,
    bus: RxBus
) : BaseViewModel<RemindersViewState>() {

    init {
        appMetricTracker.trackEvent(AppMetricEvent.TapButtonReminders)
    }

    override fun createInitState(): RemindersViewState {
        return RemindersViewState(
            reminders = emptyList()
        )
    }

    val notificationSettingsDialog = BaseDialogWidgetModel<Nothing>(
        positiveOnCLick = { sendEvent(RemindersEvent.OpenNotificationSettings) }
    )

    val alarmsAndRemindersSettingsDialog = BaseDialogWidgetModel<Nothing>(
        positiveOnCLick = { sendEvent(RemindersEvent.OpenAlarmAndRemindersSettings) }
    )

    val appTopBar = BaseAppTopBarWidgetModel()

    override val widgets = listOf(
        appTopBar,
    ).actionObserve()

    override fun handleUserAction(action: Action) {
        when (action) {
            is AppAction.BackPressure -> backClick()
            is RemindersAction.CreateReminder -> {
                appMetricTracker.trackEvent(AppMetricEvent.TapButtonRemindersNew)
                sendEvent(RemindersEvent.CheckNotificationPermission)
            }
            is RemindersAction.OpenCreateReminder -> router.navigateTo(Screens.CreateRemind)
            is RemindersAction.OpenReminder -> router.navigateTo(Screens.EditRemind(action.id))
            is RemindersAction.NotificationPermissionResult -> notificationPermissionResult(action.isGranted)
            is RemindersAction.AlarmsAndRemindersPermissionResult -> alarmsAndRemindersPermissionResult(action.isGranted)
            is RemindersAction.OpenNotificationSettingsDialog -> notificationSettingsDialog.dialogOpen()
            is RemindersAction.OpenAlarmsAndRemindersSettingsDialog -> alarmsAndRemindersSettingsDialog.dialogOpen()
        }
    }

    init {
        updateReminders()

        bus.events<Events.ReminderDeleted>()
            .map { }
            .doOnNext {
                sendEvent(RemindersEvent.ShowDeleteReminder)
            }
            .doOnNext {
                updateReminders()
            }
            .flatMap { getRemindersUseCase.execute() }
            .subscribe()

        Observable.merge(
            bus.events<Events.ReminderChanged>().map { },
            bus.events<Events.ReminderSpent>().map { }
        )
            .doOnNext {
                updateReminders()
            }
            .subscribe()

    }

    private fun updateReminders() {
        launch {
            getRemindersUseCase
                .execute().asFlow()
                .catch { handleError(it) }
                .collectLatest { reminders ->
                    reduceState {
                        state.value.copy(
                            reminders = mapper.mapFromObject(reminders)
                        )
                    }
                }
        }
    }

    private fun notificationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            sendAction(RemindersAction.OpenCreateReminder)
        }
        else {
            notificationSettingsDialog.dialogOpen()
        }

    }

    private fun alarmsAndRemindersPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            sendEvent(RemindersEvent.CheckNotificationPermission)
        }
        else {
            alarmsAndRemindersSettingsDialog.dialogOpen()
        }

    }

}