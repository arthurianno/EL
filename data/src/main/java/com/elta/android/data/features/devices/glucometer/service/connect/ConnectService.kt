package com.elta.android.data.features.devices.glucometer.service.connect

import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.GlucometerOfflineError
import com.elta.android.data.features.devices.glucometer.service.UtilService
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectService @Inject constructor(
    private val client: RxBleClient,
) {

    private val connections: MutableMap<String, RxBleConnection> = mutableMapOf()

    fun findConnection(address: String): Observable<RxBleConnection> =
        Observable.just(client.getBleDevice(address))
            .switchMap { device ->
                val connection = connections[address] //TODO надо передавать connect сразу в аргумент
                if (connection == null || device.connectionState == RxBleConnection.RxBleConnectionState.DISCONNECTED) {
                    device.establishConnection(false)
                        .onErrorResumeNext { throwable: Throwable ->
                            Timber.e(throwable, javaClass.simpleName, throwable.message)
                            when {
                                client.state == RxBleClient.State.BLUETOOTH_NOT_ENABLED -> {
                                    Observable.error(BluetoothNotEnabledError)
                                }

                                throwable is BleDisconnectedException -> {
                                    Timber.i("<<<<<<<findConnectionError>>>>>>  findConnection: $throwable")
                                    Observable.error(GlucometerOfflineError)
                                }

                                else -> Observable.error(throwable)
                            }
                        }
                        .compose(ReplayingShare.instance())
                        .doOnNext { connections[address] = it }
                } else {
                    Observable.just(connection)
                }
            }

}