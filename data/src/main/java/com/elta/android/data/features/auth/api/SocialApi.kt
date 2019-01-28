package com.elta.android.data.features.auth.api

import com.elta.android.data.features.auth.api.request.SocialNetworkRequest
import com.elta.android.data.features.auth.dto.LoginDto
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface SocialApi {

    @POST("api/auth/v1/socialnetworks/{name}/link")
    fun linkSocialNetwork(@Path("name") name: String, @Body request: SocialNetworkRequest): Completable

    @POST("api/auth/v1/socialnetworks/{name}/unlink")
    fun unLinkSocialNetwork(@Path("name") name: String): Completable

    @POST("api/auth/v1/socialnetworks/{name}/login")
    fun loginSocialNetwork(@Path("name") name: String, @Body request: SocialNetworkRequest): Single<LoginDto>
}