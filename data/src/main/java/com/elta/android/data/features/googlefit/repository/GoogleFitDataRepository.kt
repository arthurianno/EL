package com.elta.android.data.features.googlefit.repository

import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.diary.events.cache.dto.v1.DbEventsCache
import com.elta.android.data.features.googlefit.builder.EventsBuilder
import com.elta.android.data.features.googlefit.datasource.HealthAppDataSource
import com.elta.android.data.features.googlefit.datasource.errors.GoogleFitPermissionNotGranted
import com.elta.android.data.features.googlefit.datasource.errors.GoogleFitSyncNotAllowed
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.googlefit.model.BloodGlucoseData
import com.elta.android.domain.features.googlefit.model.BloodPressureData
import com.elta.android.domain.features.googlefit.model.CaloriesData
import com.elta.android.domain.features.googlefit.model.GoogleFitAuthResult
import com.elta.android.domain.features.googlefit.model.HeartRateData
import com.elta.android.domain.features.googlefit.model.HealthMetrics
import com.elta.android.domain.features.googlefit.model.WeightData
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
import timber.log.Timber
import javax.inject.Inject

class GoogleFitDataRepository @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val dataSource: HealthAppDataSource,
    private val eventsRepository: EventsRepository,
    private val eventsCache: DbEventsCache,
    private val eventsBuilder: EventsBuilder,
    private val glucoseEventsBuilder: com.elta.android.data.features.googlefit.builder.GlucoseEventsBuilder,
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
            dataSource.getBloodGlucose(),
            profileRepository.getUserId().toObservable()
        )
            .flatMapCompletable { (activities, glucoseRecords, userId) ->
                // Build events from activities (existing logic)
                val activityEvents = eventsBuilder.buildEvents(activities, userId)

                // Build events from glucose records (NEW)
                val glucoseEvents = glucoseEventsBuilder.buildGlucoseEvents(glucoseRecords, userId)

                // Combine all events
                val allEvents = activityEvents + glucoseEvents

                // Filter out existing events
                val newEvents = filterExistingEvents(allEvents)

                Timber.d("Syncing ${activityEvents.size} activities and ${glucoseEvents.size} glucose records (${newEvents.size} new)")

                if (newEvents.isEmpty()) {
                    Completable.complete()
                } else {
                    eventsRepository.addEvents(newEvents)
                        .applyScheduler(schedulersFacade)
                }
            }

    private fun filterExistingEvents(fromFit: List<EventV2>): List<EventV2> =
        if (!eventsCache.contains(CommonConditions.All)) fromFit
        else fromFit.filter { !eventsCache.contains(CommonConditions.ById(it.id.hashCode().toLong())) }

    override fun syncHealthMetrics(): Single<HealthMetrics> {
        return Single.zip(
            dataSource.getBloodGlucose()
                .map { records ->
                    records.map { record ->
                        BloodGlucoseData(
                            level = record.level.inMillimolesPerLiter,
                            time = record.time,
                            specimenSource = record.specimenSource.toString(),
                            mealType = record.mealType.toString()
                        )
                    }
                }
                .onErrorReturnItem(emptyList())
                .firstOrError(),

            dataSource.getBloodPressure()
                .map { records ->
                    records.map { record ->
                        BloodPressureData(
                            systolic = record.systolic.inMillimetersOfMercury,
                            diastolic = record.diastolic.inMillimetersOfMercury,
                            time = record.time,
                            bodyPosition = record.bodyPosition.toString()
                        )
                    }
                }
                .onErrorReturnItem(emptyList())
                .firstOrError(),

            dataSource.getWeight()
                .map { records ->
                    records.map { record ->
                        WeightData(
                            weightKg = record.weight.inKilograms,
                            time = record.time
                        )
                    }
                }
                .onErrorReturnItem(emptyList())
                .firstOrError(),

            dataSource.getHeartRate()
                .map { records ->
                    records.flatMap { record ->
                        record.samples.map { sample ->
                            HeartRateData(
                                bpm = sample.beatsPerMinute,
                                time = sample.time
                            )
                        }
                    }
                }
                .onErrorReturnItem(emptyList())
                .firstOrError(),

            dataSource.getTotalCaloriesBurned()
                .map { records ->
                    records.map { record ->
                        CaloriesData(
                            kilocalories = record.energy.inKilocalories,
                            startTime = record.startTime,
                            endTime = record.endTime
                        )
                    }
                }
                .onErrorReturnItem(emptyList())
                .firstOrError()
        ) { glucose, pressure, weight, heartRate, calories ->
            HealthMetrics(
                bloodGlucose = glucose,
                bloodPressure = pressure,
                weight = weight,
                heartRate = heartRate,
                calories = calories
            )
        }
        .doOnSuccess { metrics ->
            Timber.d("Synced health metrics: ${metrics.bloodGlucose.size} glucose, " +
                    "${metrics.bloodPressure.size} pressure, ${metrics.weight.size} weight, " +
                    "${metrics.heartRate.size} heart rate, ${metrics.calories.size} calories")
        }
        .doOnError { error ->
            Timber.e(error, "Error syncing health metrics")
        }
        .applyScheduler(schedulersFacade)
    }

    private fun disableGoogleFit(profile: Profile) = profile.copy(
        healthApps = profile.healthApps?.map { healthApp ->
            if (healthApp.type == HealthAppType.GOOGLE_FIT)
                healthApp.copy(isActive = false)
            else
                healthApp
        }
    )
}
