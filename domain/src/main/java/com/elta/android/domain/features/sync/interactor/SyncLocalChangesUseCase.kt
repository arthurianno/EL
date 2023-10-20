package com.elta.android.domain.features.sync.interactor

import com.elta.android.domain.features.diary.events.migration.EventsMigration
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.insulin.MedicinesRepository
import com.elta.android.domain.features.diary.tags.repository.TagsRepository
import com.elta.android.domain.features.googlefit.repository.GoogleFitRepository
import com.elta.android.domain.features.sale_points.repository.SalePointsRepository
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.applyScheduler
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class SyncLocalChangesUseCase @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val eventsRepo: EventsRepository,
    private val googleFitRepo: GoogleFitRepository,
    private val tagsRepository: TagsRepository,
    private val salePointsRepository: SalePointsRepository,
    private val migration: EventsMigration,
    private val medicinesRepository: MedicinesRepository,
    private val schedulers: SchedulersFacade
) : CompletableUseCase<Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Completable =
        Completable.concat(
            listOf(
                migration.migrationEventsToEventsV2(),
                profileRepo.sync(),
                eventsRepo.sync(),
                tagsRepository.sync(),
                salePointsRepository.sync(),
                medicinesRepository.sync(),
                googleFitRepo.sync()
                    .onErrorComplete(),
            )
        )
            .applyScheduler(schedulers)
}
