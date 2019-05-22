package com.elta.android.domain.features.user.repository

import com.elta.android.domain.features.user.model.Profile
import io.reactivex.Completable
import io.reactivex.Single

interface ProfileRepository {

    fun updateProfile(profile: Profile): Completable

    fun getProfile(): Single<Profile>

    fun getUserId(): Single<Long>

    fun isOnboardingPassed(): Single<Boolean>

    fun sync(): Completable
}