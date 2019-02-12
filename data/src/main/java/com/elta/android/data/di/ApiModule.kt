package com.elta.android.data.di

import android.content.Context
import com.elta.android.common.di.qualifires.Token
import com.elta.android.data.di.ApiModule.ApiConfig.USE_MOCKED_SALE_POINTS_API
import com.elta.android.data.features.auth.api.AuthApi
import com.elta.android.data.features.auth.api.SocialApi
import com.elta.android.data.features.auth.api.TokenRefreshApi
import com.elta.android.data.features.sale_points.api.MockedSalePointsApi
import com.elta.android.data.features.sale_points.api.SalePointsApi
import com.elta.android.data.features.user.api.SettingsApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@Suppress("FunctionOnlyReturningConstant", "TooManyFunctions")
class ApiModule {

    @Provides
    @Singleton
    fun provideAuthApi(
        retrofit: Retrofit
    ): AuthApi = retrofit.create<AuthApi>(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideAuthRefreshApi(
        @Token retrofit: Retrofit
    ): TokenRefreshApi = retrofit.create<TokenRefreshApi>(TokenRefreshApi::class.java)

    @Provides
    @Singleton
    fun provideAuthSocialApi(
        retrofit: Retrofit
    ): SocialApi = retrofit.create<SocialApi>(SocialApi::class.java)

    @Provides
    @Singleton
    fun provideSettingsApi(
        retrofit: Retrofit
    ): SettingsApi = retrofit.create<SettingsApi>(SettingsApi::class.java)

    @Provides
    @Singleton
    fun provideSalePointsApi(
        context: Context,
        retrofit: Retrofit
    ): SalePointsApi =
        when (USE_MOCKED_SALE_POINTS_API) {
            true -> MockedSalePointsApi(context)
            else -> retrofit.create<SalePointsApi>(SalePointsApi::class.java)
        }

    object ApiConfig {
        const val USE_MOCKED_SALE_POINTS_API = false
    }
}