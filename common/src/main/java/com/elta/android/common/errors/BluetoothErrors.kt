package com.elta.android.common.errors

object BluetoothNotAvailableError : RuntimeException()
object BluetoothNotEnabledError : RuntimeException()
object BluetoothScannerNotAvailable : RuntimeException()
object LocationPermissionNotGrantedError : RuntimeException()
// TODO Возможно стоит удалить, так как оно используется для сдк <23, а у нас 24 минимальное
object LocationNotEnabledError : RuntimeException()
object GlucometerPinRequireError : RuntimeException()

//TODO: проверить, разделиля ошибку некорректного пина и когда пина нет в базе
object GlucometerPinIncorrect : RuntimeException()
class GlucometerNotConnectedException(address: String) : RuntimeException("glucometer with address: $address not connected" )
//Исключение в случае если не удалось подключиться к устройству
class GlucometerConnectionException(address: String) : RuntimeException("can't connect to glucometer with $address" )
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
