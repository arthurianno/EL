package com.elta.android.data.features.devices.cgm.protocol

/** A validated measurement advertised by an NMG sensor. */
data class NmgAdvertisement(
    val sensorId: String,
    val state: Int,
    val uptimeSeconds: Long,
    val temperatureCode: Int,
    val glucoseSignalNanoAmp: Int,
    val currentNanoAmp: Int
) {
    val isPreparing: Boolean get() = state and STATE_PREPARING != 0
    val hasDeviceError: Boolean get() = state and STATE_ERROR != 0
    val hasLowBattery: Boolean get() = state and STATE_LOW_BATTERY != 0
    val hasInvalidTime: Boolean get() = state and STATE_INVALID_TIME != 0

    /** The protocol encodes temperature in 0.2 °C increments. It is not shown in the UI yet. */
    val temperatureCelsius: Double get() = temperatureCode * TEMPERATURE_STEP_CELSIUS

    companion object {
        const val MANUFACTURER_ID = 0xAA55
        const val ADVERTISEMENT_SIZE_BYTES = 19
        const val DATA_WITHOUT_MANUFACTURER_ID_SIZE_BYTES = 17
        const val STATE_PREPARING = 1 shl 0
        const val STATE_ERROR = 1 shl 1
        const val STATE_LOW_BATTERY = 1 shl 2
        const val STATE_INVALID_TIME = 1 shl 3
        private const val TEMPERATURE_STEP_CELSIUS = 0.2
    }
}
