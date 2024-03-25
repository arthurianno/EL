package com.elta.android.data.features.devices.mapper

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.dto.GlucometerDto
import javax.inject.Inject

class ScanResultToGlucometerDtoMapper @Inject constructor() : Mapper<ScanResult, GlucometerDto> {
    @SuppressLint("MissingPermission")
    override fun mapFromObject(source: ScanResult): GlucometerDto =
        with(source) {
            GlucometerDto(
                id = device.address,
                address = device.address,
                name = if (!device.name.isNullOrEmpty()) device.name else scanRecord?.deviceName,
                isPrimary = false
            )
        }
}
