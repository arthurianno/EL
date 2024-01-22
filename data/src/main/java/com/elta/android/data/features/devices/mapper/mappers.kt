package com.elta.android.data.features.devices.mapper

import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.domain.features.devices.model.GlucometerEvent

internal fun GlucometerEventDto.toDomain(): GlucometerEvent = GlucometerEvent(
    id = id,
    date = date,
    temperature = temperature,
    value = value,
    glucometerSerialNumber = glucometerSerialNumber,
    originalResponse = originalResponse
)