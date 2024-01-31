package com.elta.android.data.features.devices.glucometer.builder

import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.domain.features.devices.model.GlucometerEvent

interface GlucometerEventBuilder {

    fun buildFrom(
        userId: String,
        glucometerId: String,
        response: String,
        glucometerSerialNumber: String?
    ): GlucometerEvent

    fun getTimeAndValue(response: String): Pair<String, Double>
}
