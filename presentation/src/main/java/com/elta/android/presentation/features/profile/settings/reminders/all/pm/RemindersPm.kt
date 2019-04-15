package com.elta.android.presentation.features.profile.settings.reminders.all.pm

import com.elta.android.domain.features.reminder.interactor.GetRemindersUseCase
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.reminders.all.mapper.ReminderMapper
import com.nullgr.core.rx.bindEmpty
import timber.log.Timber
import javax.inject.Inject

class RemindersPm @Inject constructor(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val mapper: ReminderMapper,
    services: ServiceFacade
) : BaseListPm(services) {

    private val getReminders = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        bus.clicks<Clicks.ReminderItemClicked>()
            .doOnNext { Timber.e("clicked ${it.item.title}") }
            .subscribe()
            .untilDestroy()

        getReminders.observable
            .skipWhileInProgress()
            .flatMap {
                getRemindersUseCase.execute()
                    .bindProgress()
                    .bindEmpty(emptyControl.visibilityState.consumer)
                    .map { mapper.mapFromObject(it) }
                    .doOnNext { items.consumer.accept(it) }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        lifecycleObservable
            .filter { it == Lifecycle.CREATED }
            .map { Unit }
            .subscribe(getReminders.consumer)
            .untilDestroy()
    }
}