package com.elta.android.data.features.firmware.repository

import com.elta.android.data.features.firmware.datasource.FirmwareDataSource
import javax.inject.Inject

class FirmwareDataRepository @Inject constructor(
    private val source: FirmwareDataSource
)