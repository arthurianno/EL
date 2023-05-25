package com.elta.android.domain.features.user.repository

import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.model.ProfileSettings
import io.reactivex.Completable
import io.reactivex.Single

interface ProfileRepository {

    fun updateProfile(profile: Profile): Completable
    fun getProfile(): Single<Profile>
    fun getProfileSettings(fromCache: Boolean = true): Single<ProfileSettings>
    fun getUserId(): Single<String>
    fun sync(): Completable
    fun updateProfileSettings(
        isOnboarded: Boolean? = null,
        glucoseFormat: GlucoseFormat? = null
    ): Completable
}
