package com.elta.android.data.features.user.datasource

import io.reactivex.Completable

interface SettingsDataSource {

    fun updateUserProfile(gender: String?, weight: Double?, diabetes: String?): Completable
}