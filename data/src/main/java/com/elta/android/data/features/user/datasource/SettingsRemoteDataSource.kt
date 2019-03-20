package com.elta.android.data.features.user.datasource

import com.elta.android.data.common.checkNetwork
import com.elta.android.data.features.user.api.SettingsApi
import com.elta.android.data.features.user.api.request.ShortUserSettingsRequest
import com.elta.android.data.features.user.dto.ProfileDto
import com.nullgr.core.hardware.NetworkChecker
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class SettingsRemoteDataSource @Inject constructor(
    private val checker: NetworkChecker,
    private val api: SettingsApi
) : SettingsDataSource {

    override fun updateUserProfile(gender: String?, weight: Double?, diabetes: String?): Completable =
        api.updateUserSettings(ShortUserSettingsRequest(diabetes, weight, gender))
            .checkNetwork(checker)

    override fun getUserProfile(): Single<ProfileDto> =
        api.getUserSettings()
            .checkNetwork(checker)
}