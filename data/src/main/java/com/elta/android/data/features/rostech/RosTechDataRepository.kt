package com.elta.android.data.features.rostech

import android.app.Application
import com.elta.android.common.constants.GLUCOMETER_MODEL
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.data.common.datasource.PersonalDataStorage
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.devices.cache.dto.GlucometerInfoCachedDto
import com.elta.android.data.features.devices.glucometer.builder.GlucometerEventBuilder
import com.elta.android.domain.features.FeatureToggles
import com.elta.android.domain.features.rostech.RosTechRepository
import com.elta.android.iiot.IiotSdkDeviceService
import io.reactivex.Completable
import kotlinx.coroutines.delay
import javax.inject.Inject

class RosTechDataRepository @Inject constructor(
    private val personalData: PersonalDataStorage,
    private val application: Application,
    private val glucometerEventBuilder: GlucometerEventBuilder,
    private val glucometersInfoCache: Cache<GlucometerInfoCachedDto>,
    private val crashlyticsReport: CrashlyticsReport
): RosTechRepository {

    override fun init(): Completable {
        return if (FeatureToggles.isEnableIiotSdkFeature) {
            personalData.getIiotLogin()
                .zipWith(personalData.getIiotPassword()) { iiotSdkLogin, iiotSdkPassword ->
                    IiotSdkDeviceService.init(
                        application = application,
                        iiotSdkLogin = iiotSdkLogin,
                        iiotSdkPassword = iiotSdkPassword,
                    )
                }
                .ignoreElement()
        } else Completable.error(RosTechDisableError)
    }

    //TODO: Метод как временное решение, т.к вероятнее всего что SDK Росстеха будут ходить в глюкометр напрямую
    override suspend fun sendEvents(address: String, events: List<String>) {
        crashlyticsReport.log("sending events to SDK, feature: ${FeatureToggles.isEnableIiotSdkFeature}")
        if (FeatureToggles.isEnableIiotSdkFeature) {
            crashlyticsReport.log("start sending")
            events.forEach { event ->
                delay(20)
                IiotSdkDeviceService.sendEvent(
                    event = glucometerEventBuilder.getTimeAndValue(event),
                    serial = glucometersInfoCache.get(CommonConditions.ById(address.hashCode().toLong()))?.glucometerSerialNumber.orEmpty(),
                    model = GLUCOMETER_MODEL
                )
            }
            crashlyticsReport.log("events sent")
        }
    }
}