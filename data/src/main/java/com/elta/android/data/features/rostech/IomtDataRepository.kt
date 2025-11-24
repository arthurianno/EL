package com.elta.android.data.features.rostech

import android.app.Application
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.data.common.datasource.PersonalDataStorage
import com.elta.android.domain.features.FeatureToggles
import com.elta.android.domain.features.rostech.repository.IomtRepository
import com.elta.android.iiot.IoMTDeviceService
import io.reactivex.Completable
import java.util.concurrent.Executors
import javax.inject.Inject

class IomtDataRepository @Inject constructor(
    private val personalData: PersonalDataStorage,
    private val application: Application,
    private val crashlyticsReport: CrashlyticsReport
): IomtRepository {

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
                .doOnSuccess {
                    // TODO: тестовое решение, можем поменять 
                    IoMTDeviceService.clearLogs() 
                }
                .ignoreElement()
        } else Completable.error(RosTechDisableError)
    }

    override fun connect(pin: String, address: String, email: String) {
        Executors.newSingleThreadExecutor().submit {
            IoMTDeviceService.connect(pin, address, email)
            IoMTDeviceService.sendLogs()
        }.get()
    }

    override fun setListeners(onDisconnect: (() -> Unit)?, onException: ((Exception) -> Unit)?) {
        IoMTDeviceService.setListeners(onDisconnect, onException)
    }
}