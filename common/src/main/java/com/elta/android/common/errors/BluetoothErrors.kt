package com.elta.android.common.errors

object BluetoothNotAvailableError : RuntimeException()
object BluetoothNotEnabledError : RuntimeException()
object LocationPermissionNotGrantedError : RuntimeException()
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
object CommandError : RuntimeException()
class GlucometerSyncError(exception: Throwable) : RuntimeException(exception)
object GlucometerOfflineError : RuntimeException()
