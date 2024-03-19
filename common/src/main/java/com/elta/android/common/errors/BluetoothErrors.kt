package com.elta.android.common.errors

import com.elta.android.common.utils.hideMac

class BluetoothScannerError(val code: Int) : RuntimeException("bluetooth scanner return error: $code")
object BluetoothNotEnabledError : RuntimeException()
object BluetoothScannerNotAvailable : RuntimeException()
object LocationPermissionNotGrantedError : RuntimeException()
object LocationNotEnabledError : RuntimeException()

//TODO: проверить, разделиля ошибку некорректного пина и когда пина нет в базе
object GlucometerPinIncorrect : RuntimeException()
object GlucometerNotFoundInDfuMode : RuntimeException()
class GlucometerNotConnectedException(address: String) : RuntimeException("glucometer with address: ${address.hideMac()} not connected" )
//Исключение в случае если не удалось подключиться к устройству
class GlucometerConnectionException(address: String) : RuntimeException("can't connect to glucometer with ${address.hideMac()}" )
//TODO: проверить, разделиля ошибку некорректного пина и когда пина нет в базе
object GlucometerPinNotFoundInternaly : RuntimeException()
object GlucometerToDfuModeError : RuntimeException()
data class GlucometerLowBatteryLevelError(
    val current: Int,
    val required: Int
) : RuntimeException("Battery level $current not enough to update firmware.")

object PrimaryGlucometerNotFoundError : RuntimeException()
class CommandError(commandError: String) : RuntimeException("command error: $commandError")
class GlucometerSyncError(exception: Throwable) : RuntimeException(exception)
object GlucometerOfflineError : RuntimeException()
object GlucometerAlreadyConnectedError : RuntimeException()
