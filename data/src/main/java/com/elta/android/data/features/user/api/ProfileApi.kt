package com.elta.android.data.features.user.api

import com.elta.android.data.features.user.dto.ProfileNetworkResponse
import com.elta.android.data.features.user.dto.ProfileSettingsNetworkResponse
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH

interface ProfileApi {

    @PATCH("api/profile/v1/settings")
    @Headers("Content-Type: application/json-patch+json")
    fun updateProfile(@Body profile: ProfileNetworkResponse): Completable

    @GET("api/profile/v1/settings")
    fun getProfile(): Single<ProfileNetworkResponse>

    @GET("api/auth/v1/accounts/settings")
    fun getProfileSettings(): Single<ProfileSettingsNetworkResponse>

    @PATCH("api/auth/v1/accounts/settings")
    fun updateProfileSettings(@Body settings: ProfileSettingsNetworkResponse): Completable
}
