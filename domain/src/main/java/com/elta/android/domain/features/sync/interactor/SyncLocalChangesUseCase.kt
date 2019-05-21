package com.elta.android.domain.features.sync.interactor

import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class SyncLocalChangesUseCase @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val eventsRepo: EventsRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Completable =
        eventsRepo.sync()
            .onErrorComplete()
            .andThen(profileRepo.sync())
            .onErrorComplete()
}