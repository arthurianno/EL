package com.elta.android.domain.features.reminder.interactor

import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.repository.RemindersRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class UpdateReminderUseCase @Inject constructor(
    private val repo: RemindersRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<UpdateReminderUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable =
        repo.updateReminder(checkNotNull(params).reminder)

    data class Params(val reminder: Reminder)
}