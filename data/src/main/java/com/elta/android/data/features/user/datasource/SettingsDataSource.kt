package com.elta.android.data.features.user.datasource

import com.elta.android.data.features.user.dto.ProfileDto
import io.reactivex.Completable
import io.reactivex.Single

interface SettingsDataSource {

    fun updateUserProfile(gender: String?, weight: Double?, diabetes: String?): Completable

    fun getUserProfile(): Single<ProfileDto>
}