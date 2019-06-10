package com.elta.android.data.features.devices.glucometer

import com.elta.android.data.features.devices.dto.GlucometerEventDto

interface GlucometerEventBuilder {

    fun buildFrom(userId: String, glucometerId: String, response: String): GlucometerEventDto
}