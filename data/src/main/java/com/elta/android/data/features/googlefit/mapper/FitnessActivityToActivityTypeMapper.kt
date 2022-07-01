package com.elta.android.data.features.googlefit.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.google.android.gms.fitness.FitnessActivities
import javax.inject.Inject

class FitnessActivityToActivityTypeMapper @Inject constructor() : Mapper<String, ActivityType> {

    override fun mapFromObject(source: String): ActivityType =
        when (source) {
            FitnessActivities.ARCHERY -> ActivityType.SHOOTING
            FitnessActivities.BADMINTON -> ActivityType.BADMINTON
            FitnessActivities.BASKETBALL -> ActivityType.BASKETBALL
            FitnessActivities.BOXING -> ActivityType.BOX
            FitnessActivities.FENCING -> ActivityType.FENCING
            FitnessActivities.FOOTBALL_SOCCER -> ActivityType.FOOTBALL
            FitnessActivities.GOLF -> ActivityType.GOLF
            FitnessActivities.GYMNASTICS -> ActivityType.GYMNASTICS
            FitnessActivities.HANDBALL -> ActivityType.HANDBALL
            FitnessActivities.HIKING -> ActivityType.HIKING
            FitnessActivities.HOCKEY -> ActivityType.HOCKEY
            FitnessActivities.HORSEBACK_RIDING -> ActivityType.HORSEBACKRIDING
            FitnessActivities.MIXED_MARTIAL_ARTS -> ActivityType.WRESTLING
            FitnessActivities.SKATEBOARDING -> ActivityType.SKATEBOARDING
            FitnessActivities.SNOWBOARDING -> ActivityType.SNOWBOARDING
            FitnessActivities.TABLE_TENNIS -> ActivityType.PINGPONG
            FitnessActivities.TENNIS -> ActivityType.TENNIS
            FitnessActivities.VOLLEYBALL_BEACH -> ActivityType.BEACHVOLLEYBALL
            FitnessActivities.WALKING_NORDIC -> ActivityType.NORDICWALKING
            FitnessActivities.WATER_POLO -> ActivityType.WATERPOLO
            FitnessActivities.WHEELCHAIR -> ActivityType.WHEELCHAIRRIDING
            FitnessActivities.YOGA -> ActivityType.YOGA
            in fitnessTypes -> ActivityType.FITNESS
            in cyclingTypes -> ActivityType.CYCLING
            in dancingTypes -> ActivityType.DANCING
            in houseKeepingTypes -> ActivityType.HOUSEKEEPING
            in skatingTypes -> ActivityType.SKATING
            in rowingTypes -> ActivityType.ROWING
            in sportCombatsTypes -> ActivityType.SPORTCOMBATS
            in surfingTypes -> ActivityType.SURFING
            in walkingTypes -> ActivityType.WALKING
            in runningTypes -> ActivityType.RUNNING
            in skiingTypes -> ActivityType.SKIING
            in weightliftingTypes -> ActivityType.WEIGHTLIFTING
            in swimmingTypes -> ActivityType.SWIMMING
            in volleyballTypes -> ActivityType.VOLLEYBALL
            else -> ActivityType.ANOTHER
        }

    private val cyclingTypes = arrayListOf(
        FitnessActivities.BIKING, FitnessActivities.BIKING_HAND, FitnessActivities.BIKING_MOUNTAIN,
        FitnessActivities.BIKING_ROAD, FitnessActivities.BIKING_SPINNING, FitnessActivities.BIKING_STATIONARY,
        FitnessActivities.BIKING_UTILITY
    )

    private val fitnessTypes = arrayListOf(
        FitnessActivities.AEROBICS, FitnessActivities.HIGH_INTENSITY_INTERVAL_TRAINING,
        FitnessActivities.INTERVAL_TRAINING
    )

    private val houseKeepingTypes = arrayListOf(
        FitnessActivities.HOUSEWORK, FitnessActivities.GARDENING
    )

    private val sportCombatsTypes = arrayListOf(
        FitnessActivities.MARTIAL_ARTS, FitnessActivities.KICKBOXING
    )

    private val rowingTypes = arrayListOf(
        FitnessActivities.ROWING, FitnessActivities.KAYAKING, FitnessActivities.ROWING_MACHINE
    )

    private val runningTypes = arrayListOf(
        FitnessActivities.RUNNING, FitnessActivities.RUNNING_JOGGING, FitnessActivities.RUNNING_SAND,
        FitnessActivities.RUNNING_TREADMILL, FitnessActivities.TREADMILL
    )

    private val skatingTypes = arrayListOf(
        FitnessActivities.ICE_SKATING, FitnessActivities.SKATING, FitnessActivities.SKATING_CROSS,
        FitnessActivities.SKATING_INDOOR, FitnessActivities.SKATING_INLINE
    )

    private val skiingTypes = arrayListOf(
        FitnessActivities.SKIING, FitnessActivities.SKIING_BACK_COUNTRY, FitnessActivities.SKIING_CROSS_COUNTRY,
        FitnessActivities.SKIING_DOWNHILL, FitnessActivities.SKIING_KITE, FitnessActivities.SKIING_ROLLER
    )

    private val surfingTypes = arrayListOf(
        FitnessActivities.SURFING, FitnessActivities.KITESURFING, FitnessActivities.WINDSURFING
    )

    private val swimmingTypes = arrayListOf(
        FitnessActivities.SWIMMING, FitnessActivities.SWIMMING_OPEN_WATER, FitnessActivities.SWIMMING_POOL
    )

    private val volleyballTypes = arrayListOf(
        FitnessActivities.VOLLEYBALL, FitnessActivities.VOLLEYBALL_INDOOR
    )

    private val walkingTypes = arrayListOf(
        FitnessActivities.ON_FOOT, FitnessActivities.WALKING, FitnessActivities.WALKING_FITNESS,
        FitnessActivities.WALKING_STROLLER, FitnessActivities.WALKING_TREADMILL
    )

    private val weightliftingTypes = arrayListOf(
        FitnessActivities.WEIGHTLIFTING, FitnessActivities.STRENGTH_TRAINING
    )

    private val dancingTypes = arrayListOf(
        FitnessActivities.ZUMBA, FitnessActivities.DANCING
    )
}
