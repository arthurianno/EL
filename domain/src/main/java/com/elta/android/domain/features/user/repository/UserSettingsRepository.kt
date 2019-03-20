package com.elta.android.domain.features.user.repository

import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.domain.features.user.model.Gender
import com.elta.android.domain.features.user.model.Profile
import io.reactivex.Completable
import io.reactivex.Single

interface UserSettingsRepository {

    fun updateUserProfile(gender: Gender?, weight: Double?, diabetes: Diabetes?): Completable

    fun getProfile(): Single<Profile>
}