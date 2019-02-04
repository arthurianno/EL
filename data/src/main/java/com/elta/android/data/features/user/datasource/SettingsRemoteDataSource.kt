package com.elta.android.data.features.user.datasource

import com.elta.android.data.common.checkNetwork
import com.elta.android.data.features.user.api.SettingsApi
import com.elta.android.data.features.user.api.request.ShortUserSettingsRequest
import com.nullgr.core.hardware.NetworkChecker
import io.reactivex.Completable
import javax.inject.Inject

class SettingsRemoteDataSource @Inject constructor(
    private val checker: NetworkChecker,
    private val api: SettingsApi
) : SettingsDataSource {

    override fun updateUserProfile(gender: String?, weight: Double?, diabetes: String?): Completable =
        api.updateUserSettings(ShortUserSettingsRequest(gender, weight, diabetes))
            .checkNetwork(checker)
}