package com.elta.android.data.features.devices.glucometer.builder

import com.elta.android.data.features.devices.dto.GlucometerEventDto

interface GlucometerEventBuilder {

    fun buildFrom(
        userId: String,
        glucometerId: String,
        response: String,
        glucometerSerialNumber: String?
    ): GlucometerEventDto

    fun getTimeAndValue(response: String): Pair<String, Double>
}
