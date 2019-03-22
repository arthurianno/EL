package com.elta.android.data.features.user.api

import com.elta.android.data.features.user.dto.ProfileDto
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH

interface ProfileApi {

    @PATCH("api/profile/v1/settings")
    @Headers("Content-Type: application/json-patch+json")
    fun updateUserSettings(@Body profile: ProfileDto): Completable

    @GET("api/profile/v1/settings")
    fun getUserSettings(): Single<ProfileDto>
}