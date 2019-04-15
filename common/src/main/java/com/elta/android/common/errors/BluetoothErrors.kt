package com.elta.android.common.errors

object BluetoothNotAvailableError : RuntimeException()
object BluetoothNotEnabledError : RuntimeException()
object LocationPermissionNotGrantedError : RuntimeException()
object LocationNotEnabledError : RuntimeException()
object GlucometerPinRequireError : RuntimeException()
object GlucometerPinIncorrectOrNotFoundError : RuntimeException()
object GlucometerToDfuModeError: RuntimeException()