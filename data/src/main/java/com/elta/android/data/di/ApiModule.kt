package com.elta.android.data.di

import android.content.Context
import com.elta.android.common.di.qualifires.Token
import com.elta.android.data.features.auth.api.AuthApi
import com.elta.android.data.features.auth.api.SocialApi
import com.elta.android.data.features.auth.api.TokenRefreshApi
import com.elta.android.data.features.diary.events.api.EventsApi
import com.elta.android.data.features.diary.events.api.MockedEventsApi
import com.elta.android.data.features.diary.tags.api.MockedTagsApi
import com.elta.android.data.features.diary.tags.api.TagsApi
import com.elta.android.data.features.sale_points.api.MockedSalePointsApi
import com.elta.android.data.features.sale_points.api.SalePointsApi
import com.elta.android.data.features.user.api.MockedProfileApi
import com.elta.android.data.features.user.api.ProfileApi
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
    ): ProfileApi =
        when (ApiConfig.USE_MOCKED_SETTINGS_API) {
            true -> MockedProfileApi()
            else -> retrofit.create<ProfileApi>(ProfileApi::class.java)
        }

    @Provides
    @Singleton
    fun provideSalePointsApi(
        context: Context,
        retrofit: Retrofit
    ): SalePointsApi =
        when (ApiConfig.USE_MOCKED_SALE_POINTS_API) {
            true -> MockedSalePointsApi(context)
            else -> retrofit.create<SalePointsApi>(SalePointsApi::class.java)
        }

    @Provides
    @Singleton
    fun provideEventsApi(
        context: Context,
        retrofit: Retrofit
    ): EventsApi =
        when (ApiConfig.USE_MOCKED_EVENTS_API) {
            true -> MockedEventsApi(context)
            else -> retrofit.create<EventsApi>(EventsApi::class.java)
        }

    @Provides
    @Singleton
    fun provideTagsApi(
        context: Context,
        retrofit: Retrofit
    ): TagsApi =
        when (ApiConfig.USE_MOCKED_TAGS_API) {
            true -> MockedTagsApi(context)
            else -> retrofit.create<TagsApi>(TagsApi::class.java)
        }

    object ApiConfig {
        const val USE_MOCKED_SALE_POINTS_API = false
        const val USE_MOCKED_EVENTS_API = false
        const val USE_MOCKED_TAGS_API = false
        const val USE_MOCKED_SETTINGS_API = true
    }
}