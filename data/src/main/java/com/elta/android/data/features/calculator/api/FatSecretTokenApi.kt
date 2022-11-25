package com.elta.android.data.features.calculator.api

import com.elta.android.data.features.calculator.model.TokenResponse
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface FatSecretTokenApi {
    @POST("token")
    @FormUrlEncoded
    fun getNewToken(
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("scope") scope: String
    ): Observable<TokenResponse>
}
