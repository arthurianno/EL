package com.elta.android.domain.features.sync.interactor

import com.elta.android.common.errors.InvalidRefreshTokenError
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.tags.repository.TagsRepository
import com.elta.android.domain.features.googlefit.repository.GoogleFitRepository
import com.elta.android.domain.features.sale_points.repository.SalePointsRepository
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.applyScheduler
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import io.reactivex.functions.Predicate
import javax.inject.Inject

class SyncLocalChangesUseCase @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val eventsRepo: EventsRepository,
    private val googleFitRepo: GoogleFitRepository,
    private val tagsRepository: TagsRepository,
    private val salePointsRepository: SalePointsRepository,
    private val schedulers: SchedulersFacade
) : CompletableUseCase<Unit>(schedulers) {

    private val predicate = Predicate<Throwable> { error -> error !is InvalidRefreshTokenError }

    override fun buildUseCaseObservable(params: Unit?): Completable =
        profileRepo.sync().applyScheduler(schedulers)
            .onErrorComplete(predicate)
            .andThen(googleFitRepo.sync().applyScheduler(schedulers))
            .onErrorComplete(predicate)
            .andThen(eventsRepo.sync().applyScheduler(schedulers))
            .onErrorComplete(predicate)
            .andThen(tagsRepository.sync().applyScheduler(schedulers))
            .onErrorComplete(predicate)
            .andThen(salePointsRepository.sync().applyScheduler(schedulers))
            .onErrorComplete(predicate)
}