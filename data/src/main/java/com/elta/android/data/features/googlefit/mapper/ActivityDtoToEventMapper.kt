package com.elta.android.data.features.googlefit.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.googlefit.dto.ActivityDto
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.Event
import javax.inject.Inject

class ActivityDtoToEventMapper @Inject constructor(
    private val activityTypeMapper: Mapper<String, ActivityType>,
    private val userHolder: UserHolder
) : Mapper<ActivityDto, Event> {

    override fun mapFromObject(source: ActivityDto): Event {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}