package com.elta.android.data.features.auth.api

import com.elta.android.data.features.auth.model.AuthNetworkRequest
import com.elta.android.data.features.auth.model.ChangePasswordNetworkRequest
import com.elta.android.data.features.auth.model.EmailStatusNetworkResponse
import com.elta.android.data.features.auth.model.LoginNetworkResponse
import com.elta.android.data.features.auth.model.ResetPasswordLinkNetworkRequest
import com.elta.android.data.features.auth.model.ResetPasswordNetworkRequest
import com.elta.android.data.features.auth.model.TokenNetworkRequest
import com.elta.android.data.features.auth.model.TokenOwnerNetworkResponse
import com.elta.android.data.features.auth.model.TokensNetworkResponse
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

@Suppress("TooManyFunctions", "ComplexInterface")
interface AuthApi {

    @POST("api/auth/v1/accounts")
    fun register(@Body request: AuthNetworkRequest): Single<TokensNetworkResponse>

    @POST("api/auth/v1/accounts/login")
    fun login(@Body request: AuthNetworkRequest): Single<LoginNetworkResponse>

    @GET("api/auth/v1/accounts/email/confirmed")
    fun isEmailConfirmed(): Single<EmailStatusNetworkResponse>

    @GET("api/auth/v1/accounts/email/confirm")
    fun sendConfirmationLink(): Completable

    @PUT("api/auth/v1/accounts/password/reset")
    fun sendPasswordResetLink(@Body request: ResetPasswordLinkNetworkRequest): Completable

    @POST("api/auth/v1/accounts/password/reset")
    fun resetPassword(@Body request: ResetPasswordNetworkRequest): Completable

    @PUT("api/auth/v1/accounts/password")
    fun changePassword(@Body request: ChangePasswordNetworkRequest): Completable

    @PUT("api/auth/v1/accounts/email/confirm/token")
    fun checkTokenOwner(@Body request: TokenNetworkRequest): Single<TokenOwnerNetworkResponse>

    @POST("api/auth/v1/accounts/email/confirm")
    fun confirmEmail(@Body request: TokenNetworkRequest): Completable

    @DELETE("api/auth/v1/accounts/delete")
    fun deleteAccount(): Completable
}
