package com.elta.android.data.features.googlefit.datasource.utils

import com.elta.android.common.utils.currentMillis
import com.elta.android.data.features.googlefit.dto.ActivityDto
import com.google.android.gms.fitness.FitnessActivities
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.SessionReadRequest
import java.util.concurrent.TimeUnit

private val ignoreActivities = arrayListOf(
    FitnessActivities.SLEEP,
    FitnessActivities.SLEEP_AWAKE,
    FitnessActivities.SLEEP_DEEP,
    FitnessActivities.SLEEP_LIGHT,
    FitnessActivities.SLEEP_REM,
    FitnessActivities.STATUS_ACTIVE,
    FitnessActivities.STATUS_COMPLETED,
    FitnessActivities.STILL,
    FitnessActivities.TILTING,
    FitnessActivities.MIME_TYPE_PREFIX,
    FitnessActivities.IN_VEHICLE,
    FitnessActivities.EXTRA_STATUS
)

fun buildSessionsRequest(startTime: Long): SessionReadRequest =
    SessionReadRequest.Builder()
        .setTimeInterval(startTime, currentMillis(), TimeUnit.MILLISECONDS)
        .read(DataType.TYPE_WORKOUT_EXERCISE)
        .readSessionsFromAllApps()
        .build()

fun List<ActivityDto>.filterValidOnly(): List<ActivityDto> =
    this.filter { it.activityType !in ignoreActivities }
