package com.elta.android.domain.features.googlefit.interactor

import com.elta.android.domain.features.googlefit.model.HealthMetrics
import com.elta.android.domain.features.googlefit.repository.GoogleFitRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

/**
 * Use case for syncing health metrics from Health Connect (Android 14+)
 *
 * Returns HealthMetrics containing:
 * - Blood glucose levels
 * - Blood pressure readings
 * - Weight measurements
 * - Heart rate data
 * - Calories burned
 *
 * Example usage:
 * ```
 * syncHealthMetricsUseCase.execute()
 *     .subscribe { metrics ->
 *         // Process blood glucose
 *         metrics.bloodGlucose.forEach { glucose ->
 *             val level = glucose.level // mmol/L
 *             val time = glucose.time
 *             // Save to your database
 *         }
 *
 *         // Process weight
 *         metrics.weight.forEach { weight ->
 *             val kg = weight.weightKg
 *             val time = weight.time
 *             // Save to your database
 *         }
 *
 *         // etc...
 *     }
 * ```
 */
class SyncHealthMetricsUseCase @Inject constructor(
    private val repository: GoogleFitRepository,
    schedulersFacade: SchedulersFacade
) : SingleUseCase<HealthMetrics, Unit>(schedulersFacade) {

    override fun buildUseCaseObservable(params: Unit?): Single<HealthMetrics> {
        return repository.syncHealthMetrics()
    }
}

