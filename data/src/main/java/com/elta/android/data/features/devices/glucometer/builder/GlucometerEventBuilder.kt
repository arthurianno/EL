package com.elta.android.data.features.devices.glucometer.builder

import com.elta.android.domain.features.devices.model.GlucometerEvent
import java.util.Date

interface GlucometerEventBuilder {

    fun buildFrom(
        userId: String,
        glucometerId: String,
        response: String,
        glucometerSerialNumber: String?,
        glucometerName: String? = null
    ): GlucometerEvent

    fun getDate(response: String): Date

    fun getValue(response: String): Double
}
