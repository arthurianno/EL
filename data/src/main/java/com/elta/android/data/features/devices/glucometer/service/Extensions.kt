package com.elta.android.data.features.devices.glucometer.service

import com.elta.android.common.errors.GlucometerPinIncorrectOrNotFoundError
import com.elta.android.common.errors.GlucometerPinRequireError
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.command.Commands
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import java.nio.charset.Charset
import java.util.UUID

internal fun String.isPinError(): Boolean = this == "pin.error"
internal fun String?.isPinOk(): Boolean = this == "pin.ok"
internal fun String.isPinCommand(): Boolean = startsWith("pin")
internal fun String.isEmptyEvent(): Boolean = contains("rd000000000000000000")
internal fun String.isOk(): Boolean = endsWith("ok")
internal fun String.isError(): Boolean = contains("error")
internal fun String.isEvent(): Boolean = startsWith("rd")
internal fun GlucometerInfoDto.isBatteryLevelEnoughForUpdate(): Boolean =
    (batteryLevel ?: 0) >= MIN_BATTERY_LEVEL

internal fun Observable<RxBleConnection>.checkPinAndSend(
    pin: String?,
    request: (connection: RxBleConnection, pin: String) -> Observable<String>
): Observable<RxBleConnection> =
    this.switchMap { connection ->
        if (pin.isNullOrEmpty()) {
            Observable.error(GlucometerPinIncorrectOrNotFoundError)
        } else {
            request(connection, pin)
                .switchMap { Observable.just(connection) }
        }
    }

internal fun RxBleConnection.request(
    cmd: Commands,
    pin: String?,
    pinErrorCallback: () -> Unit,
): Observable<String> {

    val input = cmd.command
    val notification = setupNotification(UART_TX)
        .switchMap { it }
        .map { it.toString(Charset.defaultCharset()) }
    val command = writeCharacteristic(UART_RX, input.toByteArray(Charset.defaultCharset()))
        .toObservable().map { it.toString(Charset.defaultCharset()) }
    return Observables.combineLatest(notification.take(1), command) { response, _ -> response }
        .take(1)
        .compose {
            it.switchMap { response ->
                when {
                    input.isPinCommand() && response.isPinError() -> {
                        pinErrorCallback()
                        Observable.error(GlucometerPinIncorrectOrNotFoundError)
                    }

                    response.isPinError() -> Observable.error(GlucometerPinRequireError)
                    else -> Observable.just(response)
                }
            }
        }
        .retryWhen { errors ->
            errors
                .concatMap {
                    when (it) {
                        is GlucometerPinRequireError -> Observable.just(Unit)
                        else -> Observable.error(it)
                    }
                }
                .concatMap {
                    if (pin.isNullOrEmpty()) {
                        Observable.error(GlucometerPinIncorrectOrNotFoundError)
                    } else {
                        request(Commands.SetPin(pin), pin, pinErrorCallback)
                    }
                }
        }
}

internal val UART_RX = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
internal val UART_TX = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
internal const val MIN_BATTERY_LEVEL = 1