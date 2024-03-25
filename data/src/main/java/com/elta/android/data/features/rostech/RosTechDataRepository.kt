package com.elta.android.data.features.rostech

import android.app.Application
import com.elta.android.common.constants.GLUCOMETER_MODEL
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.data.common.datasource.PersonalDataStorage
import com.elta.android.data.features.devices.glucometer.builder.GlucometerEventBuilder
import com.elta.android.domain.features.FeatureToggles
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.rostech.RosTechRepository
import com.elta.android.iiot.IoMTDeviceService
import io.reactivex.Completable
import java.util.concurrent.Executors
import javax.inject.Inject

class RosTechDataRepository @Inject constructor(
    private val personalData: PersonalDataStorage,
    private val application: Application,
    private val glucometerEventBuilder: GlucometerEventBuilder,
    private val crashlyticsReport: CrashlyticsReport
): RosTechRepository {

    override fun init(): Completable {
        return if (FeatureToggles.isEnableIiotSdkFeature) {
            personalData.getIiotLogin()
                .zipWith(personalData.getIiotPassword()) { iiotSdkLogin, iiotSdkPassword ->
                    IoMTDeviceService.init(
                        application = application,
                        iiotSdkLogin = iiotSdkLogin,
                        iiotSdkPassword = iiotSdkPassword,
                        logger = crashlyticsReport
                    )
                }
                .ignoreElement()
        } else Completable.error(RosTechDisableError)
    }

    //TODO: Метод как временное решение, т.к вероятнее всего что SDK Росстеха будут ходить в глюкометр напрямую
    override fun sendMeasurements(address: String, events: List<GlucometerEvent>) {
            crashlyticsReport.log("Started sending measurements to IoMT SDK, permission to work with SDK = ${FeatureToggles.isEnableIiotSdkFeature}")
            if (FeatureToggles.isEnableIiotSdkFeature) {
                crashlyticsReport.log("Preparation of data for sending to IoMT SDK has begun")

                Executors.newSingleThreadExecutor().submit {
                    val sdkEvents = events.map {
                        IoMTDeviceService.IoMTEvent(
                            id = it.id,
                            serial = it.glucometerSerialNumber.orEmpty(),
                            model = GLUCOMETER_MODEL,
                            date = glucometerEventBuilder.getDate(it.originalResponse),
                            value = glucometerEventBuilder.getValue(it.originalResponse)
                        )
                    }
                    IoMTDeviceService.sendEvents(sdkEvents)
                }.get()

                crashlyticsReport.log("All work with the IoMT SDK has been completed successfully")
            }

    }
}