package com.elta.android.common.errors

object BluetoothNotAvailableError : RuntimeException()
object BluetoothNotEnabledError : RuntimeException()
object LocationPermissionNotGrantedError : RuntimeException()
object LocationNotEnabledError : RuntimeException()
object GlucometerPinRequireError : RuntimeException()
object GlucometerPinIncorrectOrNotFoundError : RuntimeException()
object GlucometerToDfuModeError : RuntimeException()
data class GlucometerLowBatteryLevelError(
    val current: Int,
    val required: Int
) : RuntimeException("Battery level $current not enough to update firmware.")

object PrimaryGlucometerNotFoundError : RuntimeException()