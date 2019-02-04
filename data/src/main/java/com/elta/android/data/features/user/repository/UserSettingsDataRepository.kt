package com.elta.android.data.features.user.repository

import com.elta.android.data.features.user.datasource.SettingsDataSource
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.domain.features.user.model.Gender
import com.elta.android.domain.features.user.repository.UserSettingsRepository
import io.reactivex.Completable
import javax.inject.Inject

class UserSettingsDataRepository @Inject constructor(
    private val settingsDataSource: SettingsDataSource
) : UserSettingsRepository {

    override fun updateUserProfile(gender: Gender?, weight: Double?, diabetes: Diabetes?): Completable =
        settingsDataSource.updateUserProfile(
            gender = gender?.toString(),
            weight = weight,
            diabetes = diabetes?.toString()
        )
}