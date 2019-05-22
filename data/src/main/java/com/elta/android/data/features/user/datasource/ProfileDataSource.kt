package com.elta.android.data.features.user.datasource

import com.elta.android.data.features.user.dto.ProfileDto
import io.reactivex.Completable
import io.reactivex.Single

interface ProfileDataSource {

    fun updateProfile(profile: ProfileDto): Completable

    fun getUserProfile(): Single<ProfileDto>

    fun hasProfile(): Single<Boolean>
}