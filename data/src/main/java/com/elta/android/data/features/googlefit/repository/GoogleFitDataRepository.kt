package com.elta.android.data.features.googlefit.repository

import com.elta.android.data.features.googlefit.datasource.HealthAppDataSource
import com.elta.android.data.features.googlefit.datasource.errors.GoogleFitPermissionNotGranted
import com.elta.android.data.features.googlefit.datasource.errors.GoogleFitSyncNotAllowed
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.googlefit.repository.GoogleFitRepository
import com.elta.android.domain.features.user.interactor.googleFitApp
import com.elta.android.domain.features.user.repository.ProfileRepository
import io.reactivex.Completable
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class GoogleFitDataRepository @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val dataSource: HealthAppDataSource,
    private val eventsRepository: EventsRepository
) : GoogleFitRepository {

    override fun checkAuthorization(): Observable<Boolean> = dataSource.checkAuthorization()

    override fun sync(): Completable =
        profileRepository.getProfile()
            .map { it.googleFitApp()?.isActive ?: false }
            .flatMapCompletable {
                when (it) {
                    true -> checkPermissionsAndSync()
                    else -> Completable.error(GoogleFitSyncNotAllowed)
                }
            }

    private fun checkPermissionsAndSync() =
        checkAuthorization()
            .flatMapCompletable {
                when (it) {
                    true -> syncInternal()
                    else -> Completable.error(GoogleFitPermissionNotGranted)
                }
            }

    private fun syncInternal() =
        dataSource.getActivities()
            .doOnNext { Timber.d("sync ${it.joinToString()}") }
            .ignoreElements()
}