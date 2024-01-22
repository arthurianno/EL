package com.elta.android.domain.features.devices.interactor

import com.elta.android.common.errors.GlucometerPinIncorrectOrNotFoundError
import com.elta.android.common.errors.PrimaryGlucometerNotFoundError
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.repository.DeviceInfoRepository
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.devices.repository.PinRepository
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.interactor.ObservableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.rx2.asObservable
import javax.inject.Inject

class SyncWithGlucometerUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val eventsRepository: EventsRepository,
    private val profileRepository: ProfileRepository,
    private val pinRepository: PinRepository,
    schedulers: SchedulersFacade
) : ObservableUseCase<Int, SyncWithGlucometerUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Observable<Int> {
        val device = Single.fromCallable { deviceInfoRepository.getPrimaryDevice() ?: throw PrimaryGlucometerNotFoundError }
        return Single.zip(
            device,
            profileRepository.getProfile()
        ) { (glucometer, _), profile ->
            val address = params?.device?.address ?: glucometer.address
            val email = profile.email ?: throw Exception("Email is empty")
            address to email
        }
            .flatMapObservable { (address, email) ->
                flow {
                    emit(launch(address, email))
                }.asObservable()
            }

    }


    suspend fun launch(address: String, email: String): Int {
        //TODO: Перенес логику в Use Case. Но тут требуется рефакторинг.

        val pinCode = pinRepository.getPin(address) ?: throw GlucometerPinIncorrectOrNotFoundError
        profileRepository.getProfile()
        val glucometerInfo = deviceRepository.getGlucometerInfo(address, pinCode)
        val events = deviceRepository.syncWithDevice(address, pinCode, email)

        deviceInfoRepository.updateGlucometerInfo(glucometerInfo, events.firstOrNull())
        eventsRepository.addEventFromGlucometer(events)

        return events.size
    }


    data class Params(val device: Glucometer? = null)
}
