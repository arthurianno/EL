package com.elta.android.data.features.devices.glucometer

import com.elta.android.data.features.devices.dto.GlucometerEventDto
import java.util.Date

interface GlucometerEventBuilder {

    fun buildFrom(
        userId: String,
        glucometerId: String,
        response: String,
        glucometerSerialNumber: String?
    ): GlucometerEventDto

    fun getDate(response: String): Date

    fun getValue(response: String): Double
}
