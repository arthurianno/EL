package com.elta.android.data.features.googlefit.repository

import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.diary.events.cache.DbEventsCache
import com.elta.android.data.features.googlefit.builder.EventsBuilder
import com.elta.android.data.features.googlefit.datasource.HealthAppDataSource
import com.elta.android.data.features.googlefit.datasource.errors.GoogleFitPermissionNotGranted
import com.elta.android.data.features.googlefit.datasource.errors.GoogleFitSyncNotAllowed
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.googlefit.repository.GoogleFitRepository
import com.elta.android.domain.features.user.interactor.googleFitApp
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.rx.applyScheduler
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class GoogleFitDataRepository @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val dataSource: HealthAppDataSource,
    private val eventsRepository: EventsRepository,
    private val eventsCache: DbEventsCache,
    private val eventsBuilder: EventsBuilder,
    private val schedulersFacade: SchedulersFacade
) : GoogleFitRepository {

    override fun checkAuthorization(): Observable<Boolean> = dataSource.checkAuthorization()

    override fun sync(): Completable =
        profileRepository.getProfile()
            .applyScheduler(schedulersFacade)
            .map { it.googleFitApp()?.isActive ?: false }
            .flatMapCompletable {
                when (it) {
                    true -> checkPermissionsAndSync()
                    else -> Completable.error(GoogleFitSyncNotAllowed)
                }
            }

    private fun checkPermissionsAndSync() =
        checkAuthorization()
            .take(1)
            .switchMapCompletable {
                when (it) {
                    true -> syncInternal()
                    else -> Completable.error(GoogleFitPermissionNotGranted)
                }
            }

    private fun syncInternal() =
        Observables.zip(
            dataSource.getActivities(),
            profileRepository.getUserId().toObservable()
        )
            .map { eventsBuilder.buildEvents(it.first, it.second) }
            .map(::filterExistingEvents)
            .switchMapCompletable {
                if (it.isEmpty()) Completable.complete()
                else eventsRepository.addEvents(it)
                    .applyScheduler(schedulersFacade)
            }

    private fun filterExistingEvents(fromFit: List<Event>): List<Event> =
        if (!eventsCache.contains(CommonConditions.All)) fromFit
        else fromFit.filter { !eventsCache.contains(CommonConditions.ById(it.id.hashCode().toLong())) }
}
