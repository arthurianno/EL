package com.elta.android.presentation.features.profile.settings.reminders.all.pm

import com.elta.android.domain.features.reminder.interactor.GetRemindersUseCase
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.reminders.all.mapper.ReminderMapper
import com.elta.android.presentation.messages.SnackBarMessageData
import com.nullgr.core.rx.bindEmpty
import io.reactivex.Observable
import javax.inject.Inject

class RemindersPm @Inject constructor(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val mapper: ReminderMapper,
    services: ServiceFacade
) : BaseListPm(services) {

    val newReminderAction = Action<Unit>()

    private val getReminders = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        bus.clicks<Clicks.ReminderItemClicked>()
            .map { it.item.id }
            .doOnNext { router.navigateTo(Screens.EditRemind(it)) }
            .subscribe()
            .untilDestroy()

        newReminderAction.observable
            .doOnNext { router.navigateTo(Screens.CreateRemind) }
            .subscribe()
            .untilDestroy()

        getReminders.observable
            .skipWhileInProgress()
            .flatMap {
                getRemindersUseCase.execute()
                    .bindProgress()
                    .bindEmpty(emptyControl.visibilityState.consumer)
                    .doOnNext(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        bus.events<Events.ReminderDeleted>()
            .map { Unit }
            .doOnNext(::handleReminderDelete)
            .subscribe(getReminders.consumer)
            .untilDestroy()

        Observable.merge(
            lifecycleObservable.filter { it == Lifecycle.CREATED }.map { Unit },
            bus.events<Events.EventsChanged>().map { Unit }
        )
            .subscribe(getReminders.consumer)
            .untilDestroy()
    }

    private fun handleSuccess(reminders: List<Reminder>) {
        items.consumer.accept(if (reminders.isEmpty()) emptyList() else mapper.mapFromObject(reminders))
    }

    private fun handleReminderDelete(i: Unit) {
        showSnackBar(
            SnackBarMessageData.SimpleTextMessage(
                resources.getString(R.string.profile_reminders_message_deleted)
            )
        )
    }
}