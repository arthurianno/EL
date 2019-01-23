package com.elta.android.data.features.auth.api

import com.elta.android.data.features.auth.api.request.AuthRequest
import com.elta.android.data.features.auth.api.request.RefreshRequest
import com.elta.android.data.features.auth.api.request.ResetPasswordLinkRequest
import com.elta.android.data.features.auth.api.request.ResetPasswordRequest
import com.elta.android.data.features.auth.dto.LoginDto
import com.elta.android.data.features.auth.dto.TokensDto
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/v1/accounts")
    fun register(@Body request: AuthRequest): Single<TokensDto>

    @POST("api/auth/v1/accounts/login")
    fun login(@Body request: AuthRequest): Single<LoginDto>

    @POST("api/auth/v1/accounts/refresh")
    fun refresh(@Body request: RefreshRequest): Single<TokensDto>

    @GET("api/auth/v1/accounts/email/confirmed")
    fun checkEmail(): Completable

    @GET("api/auth/v1/accounts/email/confirm")
    fun sendConfirmationLink(): Completable

    @GET("api/auth/v1/accounts/password/reset")
    fun sendPasswordResetLink(@Body request: ResetPasswordLinkRequest): Completable

    @GET("api/auth/v1/accounts/password/reset")
    fun resetPassword(@Body request: ResetPasswordRequest): Completable
}