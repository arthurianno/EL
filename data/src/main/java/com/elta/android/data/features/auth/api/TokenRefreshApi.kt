package com.elta.android.data.features.auth.api

import com.elta.android.data.features.auth.api.request.RefreshRequest
import com.elta.android.data.features.auth.dto.TokensDto
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenRefreshApi {

    @POST("api/auth/v1/accounts/refresh")
    fun refresh(@Body request: RefreshRequest): Single<TokensDto>
}
