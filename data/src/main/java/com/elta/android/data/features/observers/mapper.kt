package com.elta.android.data.features.observers // ktlint-disable filename

import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.observers.model.ObserverDbEntity
import com.elta.android.data.features.observers.model.ObserverNetworkResponse
import com.elta.android.data.features.observers.model.ObserverStatusNetworkEntity
import com.elta.android.domain.features.observers.model.Observer
import com.elta.android.domain.features.observers.model.ObserverStatus
import com.elta.android.domain.features.user.model.State

fun ObserverNetworkResponse.toDomain(): Observer =
    Observer(
        id = id,
        email = email,
        // for some reason server returns empty foodName instead of null, so we make null explicitly
        name = name?.takeIf { it.isNotBlank() },
        customName = customName,
        status = ObserverStatus.valueOf(status.name),
        modificationTime = modificationTime,
        state = State.valueOf(state.name)
    )

fun ObserverNetworkResponse.toDb(): ObserverDbEntity =
    ObserverDbEntity(
        id = id.hashCode().toLong(),
        secondaryId = id,
        email = email,
        name = name,
        customName = customName,
        status = status.name,
        modificationTime = modificationTime,
        state = state.name
    )

fun ObserverDbEntity.toNetwork(): ObserverNetworkResponse =
    ObserverNetworkResponse(
        id = secondaryId,
        email = email,
        name = name,
        customName = customName,
        status = ObserverStatusNetworkEntity.valueOf(status),
        modificationTime = modificationTime,
        state = StateDto.valueOf(state)
    )

internal fun List<ObserverNetworkResponse>.toDomain(): List<Observer> =
    map { it.toDomain() }

internal fun List<ObserverDbEntity>.toNetwork(): List<ObserverNetworkResponse> =
    map { it.toNetwork() }
