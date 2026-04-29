package com.elta.android.presentation.features.glucose.widget.worker

import android.content.Context
import android.preference.PreferenceManager
import androidx.glance.appwidget.updateAll
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.glucoseValue
import com.elta.android.domain.features.diary.home.model.GlucoseLevelDirection
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.glucose.widget.model.GlucoseWidgetData
import com.elta.android.presentation.features.glucose.widget.datasource.GlucoseWidgetPreferencesDataSource
import com.elta.android.presentation.features.glucose.widget.di.GlucoseWidgetDependencies
import com.elta.android.presentation.features.glucose.widget.ui.GlucoseAppWidget
import com.elta.android.presentation.features.glucose.widget.ui.GlucoseLargeAppWidget
import com.elta.android.presentation.features.glucose.widget.ui.GlucoseMediumAppWidget
import com.elta.android.presentation.features.glucose.widget.ui.GlucoseSmallAppWidget
import com.nullgr.core.hardware.NetworkChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Periodic worker that refreshes widget state and triggers Glance update.
 */
class GlucoseWidgetUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val dataSource = GlucoseWidgetPreferencesDataSource(appContext)
    private val networkChecker = NetworkChecker(appContext)
    private val widgetPrefs = PreferenceManager.getDefaultSharedPreferences(appContext)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        runCatching {
            val previousData = dataSource.loadWidgetData()
            val authState = resolveAuthenticatedState()
            if (authState == false) {
                dataSource.saveWidgetData(GlucoseWidgetData.unauthorized())
                updateWidget()
                return@runCatching Result.success()
            }

            val isOnline = networkChecker.isInternetConnectionEnabled()
            if (!isOnline) {
                dataSource.saveWidgetData(
                    previousData.toOfflineState(isAuthenticated = authState ?: previousData.isAuthenticated)
                )
                updateWidget()
                return@runCatching Result.success()
            }

            dataSource.saveWidgetData(fetchWidgetData(previousData))
            updateWidget()
            Result.success()
        }.getOrElse { error ->
            Timber.e(error, "GlucoseWidgetUpdateWorker failed")
            if (runAttemptCount >= MAX_RETRIES) Result.failure() else Result.retry()
        }
    }

    private fun fetchWidgetData(previousData: GlucoseWidgetData): GlucoseWidgetData {
        val homeModel = loadHomeModelSnapshot() ?: return previousData.onLoadFailed()
        val breadEvents = homeModel.eventsBlocks
            .flatMap { it.events }
            .filter { it.type is EventType.Bread }
            .sortedByDescending { it.additionTime }
        val insulinEvents = homeModel.eventsBlocks
            .flatMap { it.events }
            .filter { it.type is EventType.Insulin }
        val lastGlucose = homeModel.lastGlucoseEvent
        val glucoseTrend = homeModel.glucoseLevelDirection.toWidgetTrend()
        val glucoseDiff = homeModel.glucoseLevelDifference
            ?.toFloat()
            ?.let { diff ->
                when (homeModel.glucoseLevelDirection) {
                    GlucoseLevelDirection.DOWN -> -abs(diff)
                    GlucoseLevelDirection.UP -> abs(diff)
                    GlucoseLevelDirection.STABLE -> 0f
                    null -> diff
                }
            }
        val breadDiff = breadEvents
            .takeIf { it.size >= 2 }
            ?.let { events ->
                val current = events[0].value ?: 0.0
                val previous = events[1].value ?: 0.0
                (current - previous).toFloat()
            }
        val chartPoints = homeModel.dailyGlucoseModel.glucoseEvents
            .map { event -> event.glucoseValue(homeModel.glucoseFormat).toFloat() }

        return GlucoseWidgetData(
            glucoseValue = lastGlucose?.value?.toInt() ?: previousData.glucoseValue,
            unit = homeModel.glucoseFormat.toWidgetUnit(),
            trend = glucoseTrend ?: previousData.trend,
            breadUnits = breadEvents
                .mapNotNull { it.value }
                .sum()
                .toFloat()
                .takeIf { breadEvents.isNotEmpty() } ?: 0f,
            lastMeasurementTime = lastGlucose?.additionTime
                ?.toInstant()
                ?.toEpochMilli()
                ?.let { millis -> Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDateTime() }
                ?: previousData.lastMeasurementTime,
            syncStatus = if (chartPoints.isEmpty()) {
                GlucoseWidgetData.SyncStatus.EMPTY
            } else {
                GlucoseWidgetData.SyncStatus.SUCCESS
            },
            reminderActive = widgetPrefs.getBoolean(KEY_REMINDER_ACTIVE, false),
            reminderMessage = widgetPrefs.getString(KEY_REMINDER_MESSAGE, null),
            isAuthenticated = true,
            isOnline = true,
            glucoseDiff = glucoseDiff ?: previousData.glucoseDiff,
            breadDiff = breadDiff ?: 0f,
            insulinUnits = insulinEvents
                .mapNotNull { it.value }
                .sum()
                .toFloat()
                .takeIf { insulinEvents.isNotEmpty() } ?: 0f,
            chartPoints = chartPoints,
            reminderTimeText = widgetPrefs.getString(KEY_REMINDER_TIME, null),
            reminderDateText = widgetPrefs.getString(KEY_REMINDER_DATE, null)
        )
    }

    private fun loadHomeModelSnapshot(): HomeModel? {
        val dependencies = applicationContext.applicationContext as? GlucoseWidgetDependencies
        if (dependencies == null) {
            Timber.w("GlucoseWidgetDependencies is not provided by Application")
            return null
        }

        return runCatching {
            dependencies.getHomeModelUseCase
                .execute()
                .firstOrError()
                .timeout(HOME_MODEL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .blockingGet()
        }.onFailure {
            Timber.e(it, "Failed to load HomeModel for widget")
        }.getOrNull()
    }

    private fun resolveAuthenticatedState(): Boolean? {
        val rawValue = widgetPrefs.all[KEY_CURRENT_USER] ?: return null
        val userId = when (rawValue) {
            is Long -> rawValue
            is Int -> rawValue.toLong()
            is String -> rawValue.toLongOrNull() ?: return null
            else -> return null
        }
        return userId != NO_USER_ID
    }

    private suspend fun updateWidget() {
        GlucoseSmallAppWidget().updateAll(applicationContext)
        GlucoseMediumAppWidget().updateAll(applicationContext)
        GlucoseLargeAppWidget().updateAll(applicationContext)
    }


    companion object {
        const val WORK_NAME = "glucose_widget_update"
        private const val IMMEDIATE_WORK_NAME = "glucose_widget_update_immediate"

        private const val REPEAT_INTERVAL_MINUTES = 15L
        private const val FLEX_INTERVAL_MINUTES = 5L
        private const val MAX_RETRIES = 3
        private const val HOME_MODEL_TIMEOUT_SECONDS = 20L

        // SharedPreferences keys
        private const val KEY_CURRENT_USER = "current_user"
        private const val NO_USER_ID = -1L
        private const val KEY_REMINDER_ACTIVE = "reminder_active"
        private const val KEY_REMINDER_MESSAGE = "reminder_message"
        private const val KEY_REMINDER_TIME = "reminder_time"
        private const val KEY_REMINDER_DATE = "reminder_date"

        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<GlucoseWidgetUpdateWorker>(
                REPEAT_INTERVAL_MINUTES, TimeUnit.MINUTES,
                FLEX_INTERVAL_MINUTES, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun requestImmediateUpdate(context: Context) {
            val request = OneTimeWorkRequestBuilder<GlucoseWidgetUpdateWorker>()
                .addTag(IMMEDIATE_WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

private fun GlucoseWidgetData.toOfflineState(isAuthenticated: Boolean): GlucoseWidgetData =
    if (hasPrimaryData()) {
        copy(
            syncStatus = GlucoseWidgetData.SyncStatus.OFFLINE,
            isOnline = false,
            isAuthenticated = isAuthenticated
        )
    } else {
        GlucoseWidgetData.offline()
    }

private fun GlucoseWidgetData.onLoadFailed(): GlucoseWidgetData =
    if (hasPrimaryData()) {
        copy(syncStatus = GlucoseWidgetData.SyncStatus.FAILED)
    } else {
        GlucoseWidgetData.noData()
    }

private fun GlucoseWidgetData.hasPrimaryData(): Boolean =
    glucoseValue != null || breadUnits != null || chartPoints.isNotEmpty()

private fun GlucoseFormat.toWidgetUnit(): GlucoseWidgetData.GlucoseUnit =
    when (this) {
        GlucoseFormat.CAPILLARY,
        GlucoseFormat.PLASMA -> GlucoseWidgetData.GlucoseUnit.MMOL_L
    }

private fun GlucoseLevelDirection?.toWidgetTrend(): GlucoseWidgetData.GlucoseTrend? =
    when (this) {
        GlucoseLevelDirection.UP -> GlucoseWidgetData.GlucoseTrend.UP
        GlucoseLevelDirection.DOWN -> GlucoseWidgetData.GlucoseTrend.DOWN
        GlucoseLevelDirection.STABLE -> GlucoseWidgetData.GlucoseTrend.STABLE
        null -> null
    }
