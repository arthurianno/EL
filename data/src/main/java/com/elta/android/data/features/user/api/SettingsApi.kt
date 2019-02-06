package com.elta.android.data.features.user.api

import com.elta.android.data.features.user.api.request.ShortUserSettingsRequest
import io.reactivex.Completable
import retrofit2.http.Body
import retrofit2.http.PATCH

interface SettingsApi {

    @PATCH("api/profile/v1/settings")
    fun updateUserSettings(@Body request: ShortUserSettingsRequest): Completable
}