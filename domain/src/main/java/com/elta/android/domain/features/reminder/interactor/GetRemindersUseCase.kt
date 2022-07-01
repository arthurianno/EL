package com.elta.android.domain.features.reminder.interactor

import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.repository.RemindersRepository
import com.nullgr.core.interactor.ObservableListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class GetRemindersUseCase @Inject constructor(
    private val repo: RemindersRepository,
    schedulers: SchedulersFacade
) : ObservableListUseCase<Reminder, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Observable<List<Reminder>> =
        repo.getReminders()
            .map { it.sortByTime() }
}
