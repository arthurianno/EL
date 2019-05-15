package com.elta.android.data.features.devices.glucometer

import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import java.util.Date

interface GlucometerInfoBuilder {

    fun buildFrom(params: List<String>, syncDate: Date? = null): GlucometerInfoDto
}