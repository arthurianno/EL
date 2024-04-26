package com.elta.android.data.features.glucometers.mapper

import com.elta.android.data.features.glucometers.dto.GlucometerNetworkEntity
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo

fun List<Pair<Glucometer, GlucometerInfo>>.toNM(): GlucometerNetworkEntity {

    val list = partition { (glucometer, _) -> glucometer.isPrimary }
    val primaryList = list.first
        .toGlucometerInfoNM()

    val secondaryList = list.second.toGlucometerInfoNM()

    val primary = primaryList
        .firstOrNull()

    return GlucometerNetworkEntity(
        primaryGlucometer = primary,
        secondaryGlucometers = secondaryList
    )
}

private fun List<Pair<Glucometer, GlucometerInfo>>.toGlucometerInfoNM(): List<GlucometerNetworkEntity.GlucometerInfoNetworkEntity> {

    return map { (glucometer, glucometerInfo) ->
        GlucometerNetworkEntity.GlucometerInfoNetworkEntity(
            serialNumber = glucometerInfo.glucometerSerialNumber.orEmpty(),
            hardwareVersion = glucometerInfo.hardwareVersion.orEmpty(),
            firmwareVersion = glucometerInfo.softwareVersion.orEmpty(),
            mac = glucometer.address.filterNot { it == MAC_SEPARATOR },
        )
    }
}

private const val MAC_SEPARATOR = ':'
