package com.elta.android.data.features.devices.glucometer.builder

import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.domain.features.devices.model.GlucometerInfo

interface GlucometerInfoBuilder {

    fun buildFrom(glucometerInfo: GlucometerInfo, lastSyncedEvent: String?): GlucometerInfoDto
}
