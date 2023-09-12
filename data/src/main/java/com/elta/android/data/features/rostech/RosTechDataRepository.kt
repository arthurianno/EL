package com.elta.android.data.features.rostech

import android.app.Application
import com.elta.android.data.common.datasource.PersonalDataStorage
import com.elta.android.domain.features.FeatureToggles
import com.elta.android.domain.features.rostech.RosTechRepository
import com.elta.android.iiot.IiotSdkDeviceService
import io.reactivex.Completable
import javax.inject.Inject

class RosTechDataRepository @Inject constructor(
    private val personalData: PersonalDataStorage,
    private val application: Application
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
}