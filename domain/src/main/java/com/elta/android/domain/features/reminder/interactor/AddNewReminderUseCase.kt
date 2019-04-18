package com.elta.android.domain.features.reminder.interactor

import com.elta.android.domain.features.reminder.model.ScheduleType
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.repository.RemindersRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class AddNewReminderUseCase @Inject constructor(
    private val repo: RemindersRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<AddNewReminderUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        val p = checkNotNull(params)
        return repo.addReminder(
            Reminder(
                id = UUID.randomUUID().toString(),
                title = p.title,
                time = p.date,
                scheduleType = p.schedule
            )
        )
    }

    data class Params(
        val title: String,
        val date: Date?,
        val schedule: ScheduleType
    )
}