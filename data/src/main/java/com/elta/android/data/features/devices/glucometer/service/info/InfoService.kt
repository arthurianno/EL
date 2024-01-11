package com.elta.android.data.features.devices.glucometer.service.info

import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.builder.GlucometerInfoBuilder
import com.elta.android.data.features.devices.glucometer.command.Commands
import com.elta.android.data.features.devices.glucometer.service.UtilService
import com.elta.android.data.features.devices.glucometer.service.checkPinAndSend
import com.elta.android.data.features.devices.glucometer.service.connect.ConnectService
import com.elta.android.data.features.devices.glucometer.service.request
import com.elta.android.data.features.devices.glucometer.storage.GlucometerPinStorage
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InfoService @Inject constructor(
    private val connectService: ConnectService, //TODO: убрать зависимости и передавать в аргументах
    private val pinStorage: GlucometerPinStorage, //TODO: убрать зависимости и передавать в аргументах
    private val utilService: UtilService, //TODO: убрать зависимости и передавать в аргументах
    private val infoBuilder: GlucometerInfoBuilder,
) {

    fun fetchGlucometerInfo(address: String): Single<GlucometerInfoDto> = //TODO: передавать сразу CONNECTION!
        connectService.findConnection(address)
            .checkPinAndSend(pinStorage.getPin(address)) { connection, pin ->
                utilService.request(
                    connection,
                    address,
                    Commands.SetPin(pin)
                )
            }
            .switchMap { connection ->
                val commands = utilService.infoCommands
                Observable.fromIterable(commands.map { cmd ->
                    connection.request(
                        cmd = cmd,
                        pin = pinStorage.getPin(address),
                        pinErrorCallback = { pinStorage.setPin(address, "") },
                    )
                })
                    .concatMap { it }
                    .buffer(commands.size)
            }
            .take(1)
            .map { infoBuilder.buildFrom(address, it) }
            .singleOrError()

}
