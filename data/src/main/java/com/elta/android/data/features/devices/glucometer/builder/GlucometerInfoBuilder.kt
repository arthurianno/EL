package com.elta.android.data.features.devices.glucometer.builder

import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.dto.VersionDto
import com.elta.android.domain.features.devices.model.GlucometerInfo
import org.threeten.bp.ZonedDateTime

interface GlucometerInfoBuilder {

    @Deprecated("Used for old synchronization and firmware updates. Use the method with other parameters.")
    fun buildFrom(
        id: String,
        params: List<String>,
        syncDate: ZonedDateTime? = null,
        lastSyncedEvent: String? = null
    ): GlucometerInfoDto

    fun buildFrom(glucometerInfo: GlucometerInfo, lastSyncedEvent: String?): GlucometerInfoDto
}
