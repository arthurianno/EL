package com.elta.android.domain.features.reminder.interactor

import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.repository.RemindersRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetReminderByIdUseCase @Inject constructor(
    private val repo: RemindersRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<Reminder, GetReminderByIdUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<Reminder> =
        repo.getReminderById(checkNotNull(params).id)

    data class Params(val id: String)
}
