package com.elta.android.domain.features.reminder.interactor

import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.repository.RemindersRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class AddNewReminderUseCase @Inject constructor(
    private val repo: RemindersRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<String, AddNewReminderUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<String> {
        val p = checkNotNull(params)
        return repo.addReminder(p.reminder)
    }

    data class Params(val reminder: Reminder)
}
