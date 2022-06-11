package com.elta.android.data.features.devices.glucometer

import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import org.threeten.bp.ZonedDateTime

interface GlucometerInfoBuilder {

    fun buildFrom(
        id: String,
        params: List<String>,
        syncDate: ZonedDateTime? = null,
        lastSyncedEvent: String? = null
    ): GlucometerInfoDto
}
