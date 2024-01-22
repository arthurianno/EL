package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.devices.repository.GlucometerRepository
import com.elta.android.domain.features.devices.repository.PinRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxObservable
import javax.inject.Inject

class AddNewDeviceUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val pinRepository: PinRepository,
    private val glucometerRepository: GlucometerRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<AddNewDeviceUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable {

        return flow {
            emit(test(params))
        }.asObservable()
            .ignoreElements()

//        return rxObservable {
//            send(test(params))
//        }
//            .ignoreElements()
    }

    suspend fun test(params: Params?) {
        val p = checkNotNull(params)
        val address = p.device.address
        deviceRepository.connectDevice(address, p.pinCode)
        pinRepository.savePin(address, p.pinCode)

        val primaryDevice = glucometerRepository.getPrimaryDevice()
        if (primaryDevice == null) {
            glucometerRepository.putDevice(p.device, true)
        }

        if (primaryDevice != null && !primaryDevice.address.equals(address, true)) {
            glucometerRepository.putDevice(p.device, false)
        }
    }

    data class Params(val device: Glucometer, val pinCode: String)
}
