package com.elta.android.data.features.devices.glucometer

import com.elta.android.data.features.devices.dto.GlucometerInfoDto

interface GlucometerInfoBuilder {

    fun buildFrom(params: List<String>): GlucometerInfoDto
}