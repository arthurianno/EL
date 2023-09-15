package com.elta.android.data.features.auth.api

import com.elta.android.data.features.auth.model.LoginNetworkResponse
import com.elta.android.data.features.auth.model.SocialNetworkRequest
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface SocialApi {

    @POST("api/auth/v1/socialnetworks/{foodName}/link")
    fun linkSocialNetwork(
        @Path("foodName") name: String,
        @Body request: SocialNetworkRequest
    ): Completable

    @POST("api/auth/v1/socialnetworks/{foodName}/unlink")
    fun unLinkSocialNetwork(@Path("foodName") name: String): Completable

    @POST("api/auth/v1/socialnetworks/{foodName}/login")
    fun loginSocialNetwork(
        @Path("foodName") name: String,
        @Body request: SocialNetworkRequest
    ): Single<LoginNetworkResponse>
}
