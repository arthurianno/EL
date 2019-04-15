package com.elta.android.domain.features.reminder.interactor

import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.repository.RemindersRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class DeleteReminderUseCase @Inject constructor(
    private val repo: RemindersRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<DeleteReminderUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable =
        repo.deleteReminder(checkNotNull(params).reminder)

    data class Params(val reminder: Reminder)
}