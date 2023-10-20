package com.elta.android.data.features.googlefit.repository

import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.diary.events.cache.dto.v1.DbEventsCache
import com.elta.android.data.features.googlefit.builder.EventsBuilder
import com.elta.android.data.features.googlefit.datasource.HealthAppDataSource
import com.elta.android.data.features.googlefit.datasource.errors.GoogleFitPermissionNotGranted
import com.elta.android.data.features.googlefit.datasource.errors.GoogleFitSyncNotAllowed
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.googlefit.model.GoogleFitAuthResult
import com.elta.android.domain.features.googlefit.repository.GoogleFitRepository
import com.elta.android.domain.features.user.interactor.googleFitApp
import com.elta.android.domain.features.user.model.HealthAppType
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.rx.applyScheduler
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import io.reactivex.Single
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

    override fun checkAuthorization(): Single<GoogleFitAuthResult> = dataSource.checkAuthorization()

    override fun sync(): Completable =
        profileRepository.getProfile()
            .applyScheduler(schedulersFacade)
            .flatMapCompletable { profile ->
                when (profile.googleFitApp()?.isActive ?: false) {
                    true -> checkPermissionsAndSync(profile)
                    else -> Completable.error(GoogleFitSyncNotAllowed)
                }
            }

    private fun checkPermissionsAndSync(profile: Profile) =
        checkAuthorization()
            .flatMapCompletable {
                when (it) {
                    GoogleFitAuthResult.Access -> syncInternal()
                    GoogleFitAuthResult.ApplicationNotInstalled, GoogleFitAuthResult.NotAccess -> {
                        val updateProfile = disableGoogleFit(profile)
                        profileRepository.updateProfile(updateProfile)
                            .andThen(Completable.error(GoogleFitPermissionNotGranted))
                    }
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

    private fun filterExistingEvents(fromFit: List<EventV2>): List<EventV2> =
        if (!eventsCache.contains(CommonConditions.All)) fromFit
        else fromFit.filter { !eventsCache.contains(CommonConditions.ById(it.id.hashCode().toLong())) }

    private fun disableGoogleFit(profile: Profile) = profile.copy(
        healthApps = profile.healthApps?.map { healthApp ->
            if (healthApp.type == HealthAppType.GOOGLE_FIT)
                healthApp.copy(isActive = false)
            else
                healthApp
        }
    )
}
