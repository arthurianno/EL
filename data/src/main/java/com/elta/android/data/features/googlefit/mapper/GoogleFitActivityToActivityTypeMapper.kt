package com.elta.android.data.features.googlefit.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.domain.features.diary.events.model.ActivityType
import javax.inject.Inject

class GoogleFitActivityToActivityTypeMapper @Inject constructor() : Mapper<String, ActivityType>{

    override fun mapFromObject(source: String): ActivityType {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}