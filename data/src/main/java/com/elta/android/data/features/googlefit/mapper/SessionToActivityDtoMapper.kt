package com.elta.android.data.features.googlefit.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.googlefit.dto.ActivityDto
import com.google.android.gms.fitness.data.Session
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SessionToActivityDtoMapper @Inject constructor() : Mapper<Session, ActivityDto> {
    override fun mapFromObject(source: Session): ActivityDto =
        ActivityDto(
            id = source.identifier,
            activityType = source.activity,
            duration = source.getActiveTime(TimeUnit.MILLISECONDS),
            startTime = source.getStartTime(TimeUnit.MILLISECONDS)
        )
}