package com.elta.android.data.features.observers.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.observers.dto.ObserverDto
import com.elta.android.domain.features.observers.model.Observer
import com.elta.android.domain.features.observers.model.ObserverStatus
import com.elta.android.domain.features.user.model.State
import javax.inject.Inject

class ObserverToDomainMapper @Inject constructor() : Mapper<ObserverDto, Observer> {
    override fun mapFromObject(source: ObserverDto): Observer =
        with(source) {
            Observer(
                id = id,
                email = email,
                name = name,
                customName = customName,
                status = ObserverStatus.valueOf(status.name),
                modificationTime = modificationTime,
                state = State.valueOf(state.name)
            )
        }
}