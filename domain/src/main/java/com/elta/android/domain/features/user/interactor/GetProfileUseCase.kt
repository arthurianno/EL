package com.elta.android.domain.features.user.interactor

import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.applyScheduler
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val userRepo: ProfileRepository,
    private val eventsRepo: EventsRepository,
    private val schedulers: SchedulersFacade
) : SingleUseCase<Profile, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Single<Profile> =
        Singles.zip(
            userRepo.getProfile().applyScheduler(schedulers),
            eventsRepo.getEvents().singleOrError().onErrorReturn { emptyList() }.applyScheduler(schedulers)
        ).map { buildProfile(it.first, it.second) }
}
