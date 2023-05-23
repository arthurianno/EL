package com.elta.android.data.features.user.datasource

import com.elta.android.data.features.user.dto.ProfileNetworkResponse
import com.elta.android.data.features.user.dto.ProfileSettingsNetworkRequest
import com.elta.android.data.features.user.dto.ProfileSettingsNetworkResponse
import io.reactivex.Completable
import io.reactivex.Single

interface ProfileDataSource {
    fun updateProfile(profile: ProfileNetworkResponse): Completable
    fun getUserProfile(): Single<ProfileNetworkResponse>
    fun hasProfile(): Single<Boolean>
    fun getProfileSettings(): Single<ProfileSettingsNetworkResponse>
    fun updateProfileSettings(settings: ProfileSettingsNetworkRequest): Completable
}
