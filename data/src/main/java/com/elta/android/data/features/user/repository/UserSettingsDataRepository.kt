package com.elta.android.data.features.user.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.data.features.user.datasource.SettingsDataSource
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.domain.features.user.model.Gender
import com.elta.android.domain.features.user.repository.UserSettingsRepository
import io.reactivex.Completable
import javax.inject.Inject

class UserSettingsDataRepository @Inject constructor(
    @Cache private val cachedSource: SettingsDataSource,
    @Remote private val remoteSource: SettingsDataSource
) : UserSettingsRepository {

    override fun updateUserProfile(gender: Gender?, weight: Double?, diabetes: Diabetes?): Completable =
        remoteSource.updateUserProfile(
            gender = gender?.name,
            weight = weight,
            diabetes = diabetes?.name
        )
}