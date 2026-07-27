package com.elta.android.common.errors

import com.elta.android.common.utils.hideMac

class BluetoothScannerError(val code: Int) : RuntimeException("bluetooth scanner return error: $code")
object BluetoothNotEnabledError : RuntimeException()
object BluetoothScannerNotAvailable : RuntimeException()
// fixme Variant A : improved_enabling_location
object LocationPermissionNotGrantedErrorVariantA : RuntimeException()
object BluetoothNotEnabledErrorVariantA : RuntimeException()
object LocationNotEnabledErrorVariantA : RuntimeException()

object LocationPermissionNotGrantedError : RuntimeException()
object BluetoothPermissionNotGrantedError : RuntimeException()
object LocationNotEnabledError : RuntimeException()

object GlucometerPinIncorrect : RuntimeException()
object GlucometerNotFoundInDfuMode : RuntimeException()
class GlucometerNotConnectedException(address: String) : RuntimeException("glucometer with address: ${address.hideMac()} not connected" )
//Исключение в случае если не удалось подключиться к устройству
class GlucometerConnectionException(address: String) : RuntimeException("can't connect to glucometer with ${address.hideMac()}" )
object GlucometerPinNotFoundInternaly : RuntimeException()
object GlucometerToDfuModeError : RuntimeException()
data class GlucometerLowBatteryLevelError(
    val current: Int,
    val required: Int
) : RuntimeException("Battery level $current not enough to update firmware.")

object PrimaryGlucometerNotFoundError : RuntimeException()
class CommandError(commandError: String) : RuntimeException("command error: $commandError")
object CommandStillWritingError : RuntimeException()
class GlucometerSyncError(exception: Throwable) : RuntimeException(exception)
object GlucometerOfflineError : RuntimeException()
object GlucometerAlreadyConnectedError : RuntimeException()
object GlucometerDeviceHardwareError : RuntimeException()

object GlucometerTestConfig {
    var MOCK_HARDWARE_ERROR: Boolean = false
}


