package com.elta.android.data.features.googlefit.mapper

import androidx.health.connect.client.records.ExerciseSessionRecord
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.googlefit.dto.ActivityDto
import javax.inject.Inject

/**
 * Maps Health Connect ExerciseSessionRecord to ActivityDto
 */
class HealthConnectExerciseToActivityMapper @Inject constructor() : Mapper<ExerciseSessionRecord, ActivityDto> {

    override fun mapFromObject(source: ExerciseSessionRecord): ActivityDto {
        val durationMillis = source.endTime.toEpochMilli() - source.startTime.toEpochMilli()
        return ActivityDto(
            id = source.metadata.id.hashCode().toString(),
            activityType = mapExerciseTypeToString(source.exerciseType),
            duration = durationMillis / 1000, // Convert milliseconds to seconds
            additionTime = source.startTime.toEpochMilli(),
            note = source.notes ?: source.title ?: ""
        )
    }

    override fun mapFromObjects(sources: Collection<ExerciseSessionRecord>): List<ActivityDto> {
        return sources.map { mapFromObject(it) }
    }

    private fun mapExerciseTypeToString(exerciseType: Int): String {
        // Health Connect exercise types: https://developer.android.com/reference/kotlin/androidx/health/connect/client/records/ExerciseSessionRecord
        return when (exerciseType) {
            // Walking & Running
            ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "walking"
            ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "running"
            ExerciseSessionRecord.EXERCISE_TYPE_RUNNING_TREADMILL -> "running_treadmill"

            // Cycling
            ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> "biking"
            ExerciseSessionRecord.EXERCISE_TYPE_BIKING_STATIONARY -> "biking_stationary"

            // Swimming
            ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> "swimming_open_water"
            ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> "swimming_pool"

            // Fitness
            ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "yoga"
            ExerciseSessionRecord.EXERCISE_TYPE_PILATES -> "pilates"
            ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> "strength_training"
            ExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING -> "weightlifting"
            ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS -> "calisthenics"
            ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> "hiit"

            // Sports
            ExerciseSessionRecord.EXERCISE_TYPE_FOOTBALL_AMERICAN -> "football_american"
            ExerciseSessionRecord.EXERCISE_TYPE_FOOTBALL_AUSTRALIAN -> "football_australian"
            ExerciseSessionRecord.EXERCISE_TYPE_BASKETBALL -> "basketball"
            ExerciseSessionRecord.EXERCISE_TYPE_VOLLEYBALL -> "volleyball"
            ExerciseSessionRecord.EXERCISE_TYPE_TENNIS -> "tennis"
            ExerciseSessionRecord.EXERCISE_TYPE_BADMINTON -> "badminton"
            ExerciseSessionRecord.EXERCISE_TYPE_TABLE_TENNIS -> "table_tennis"

            // Default
            else -> "other"
        }
    }
}

