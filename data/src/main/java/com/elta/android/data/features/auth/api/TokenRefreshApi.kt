package com.elta.android.data.features.auth.api

import com.elta.android.data.features.auth.model.RefreshNetworkRequest
import com.elta.android.data.features.auth.model.TokensNetworkResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenRefreshApi {

    @POST("api/auth/v1/accounts/refresh")
    fun refresh(@Body request: RefreshNetworkRequest): Call<TokensNetworkResponse>
}
