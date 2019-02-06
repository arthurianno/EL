package com.elta.android.domain.features.user.repository

import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.domain.features.user.model.Gender
import io.reactivex.Completable

interface UserSettingsRepository {

    fun updateUserProfile(gender: Gender?, weight: Double?, diabetes: Diabetes?): Completable
}