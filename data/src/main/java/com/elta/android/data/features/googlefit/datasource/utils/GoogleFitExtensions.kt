package com.elta.android.data.features.googlefit.datasource.utils

import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.SessionReadRequest
import java.util.concurrent.TimeUnit

fun makeFitnessOptions(): FitnessOptions =
    FitnessOptions.builder()
        .addDataType(DataType.TYPE_ACTIVITY_SAMPLES, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_WORKOUT_EXERCISE, FitnessOptions.ACCESS_READ)
        .build()

fun buildSessionsRequest(startTime: Long): SessionReadRequest =
    SessionReadRequest.Builder()
        .setTimeInterval(startTime, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        .read(DataType.TYPE_WORKOUT_EXERCISE)
        .readSessionsFromAllApps()
        .build()