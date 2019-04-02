package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.model.Command
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetGlucometerInfo @Inject constructor(
    private val repo: DeviceRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<GlucometerInfo, GetGlucometerInfo.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<GlucometerInfo> =
        repo.getDeviceInfo(checkNotNull(params).address, checkNotNull(params).fields)

    private fun Params?.validate(): Params {
        if (this == null) throw NullPointerException("Params can't be null.")
        fields.forEach { command ->
            if (command !in supportedCommands) {
                throw IllegalArgumentException("Device doesn't support ${command::class.java.simpleName} for getting GlucometerInfo")
            }
        }
        return this
    }

    data class Params(
        val address: String,
        val fields: List<Command>
    )


    companion object {
        private val supportedCommands = listOf(
            Command.GetVersion,
            Command.GetBatteryAndTemperature,
            Command.GetDate
        )
    }
}