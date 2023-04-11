package com.elta.android.data.di

import android.content.Context
import com.elta.android.common.di.qualifires.FatSecret
import com.elta.android.common.di.qualifires.FatSecretAnnotationType
import com.elta.android.common.di.qualifires.Token
import com.elta.android.data.common.api.PersonalDataApi
import com.elta.android.data.common.api.PersonalDataMockedApi
import com.elta.android.data.features.auth.api.AuthApi
import com.elta.android.data.features.auth.api.SocialApi
import com.elta.android.data.features.auth.api.TokenRefreshApi
import com.elta.android.data.features.calculator.api.CalculatorApi
import com.elta.android.data.features.calculator.api.CalculatorMockedApi
import com.elta.android.data.features.calculator.api.FatSecretApi
import com.elta.android.data.features.calculator.api.FatSecretTokenApi
import com.elta.android.data.features.diary.events.api.EventsApi
import com.elta.android.data.features.diary.events.api.MockedEventsApi
import com.elta.android.data.features.diary.insulin.api.DrugNameApi
import com.elta.android.data.features.diary.insulin.api.MockedDrugNameApi
import com.elta.android.data.features.diary.tags.api.MockedTagsApi
import com.elta.android.data.features.diary.tags.api.TagsApi
import com.elta.android.data.features.feedback.api.FeedbackApi
import com.elta.android.data.features.feedback.api.MockedFeedbackApi
import com.elta.android.data.features.firmware.api.FirmwareApi
import com.elta.android.data.features.firmware.api.MockedFirmwareApi
import com.elta.android.data.features.observers.api.MockedObserverApi
import com.elta.android.data.features.observers.api.ObserverApi
import com.elta.android.data.features.reports.api.MockedReportsApi
import com.elta.android.data.features.reports.api.ReportsApi
import com.elta.android.data.features.sale_points.api.MockedSalePointsApi
import com.elta.android.data.features.sale_points.api.SalePointsApi
import com.elta.android.data.features.user.api.MockedProfileApi
import com.elta.android.data.features.user.api.ProfileApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

private const val USE_MOCKED_SALE_POINTS_API = false
private const val USE_MOCKED_EVENTS_API = false
private const val USE_MOCKED_TAGS_API = false
private const val USE_MOCKED_SETTINGS_API = false
private const val USE_MOCKED_OBSERVER_API = false
private const val USE_MOCKED_FIRMWARE_API = false
private const val USE_MOCKED_FEEDBACK_API = false
private const val USE_MOCKED_REPORTS_API = false
private const val USE_MOCKED_INSULIN_DRUG_API = false
private const val USE_MOCKED_CALCULATOR_API = false
private const val USE_MOCKED_PERSONAL_DATA_API = true

@Module
@Suppress("FunctionOnlyReturningConstant", "TooManyFunctions")
class ApiModule {

    @Provides
    @Singleton
    fun provideAuthApi(
        retrofit: Retrofit
    ): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideAuthRefreshApi(
        @Token retrofit: Retrofit
    ): TokenRefreshApi = retrofit.create(TokenRefreshApi::class.java)

    @Provides
    @Singleton
    fun provideAuthSocialApi(
        retrofit: Retrofit
    ): SocialApi = retrofit.create(SocialApi::class.java)

    @Provides
    @Singleton
    fun provideSettingsApi(
        retrofit: Retrofit
    ): ProfileApi =
        if (USE_MOCKED_SETTINGS_API) {
            MockedProfileApi()
        } else {
            retrofit.create(ProfileApi::class.java)
        }

    @Provides
    @Singleton
    fun provideObserverApi(
        retrofit: Retrofit
    ): ObserverApi =
        if (USE_MOCKED_OBSERVER_API) {
            MockedObserverApi()
        } else {
            retrofit.create(ObserverApi::class.java)
        }

    @Provides
    @Singleton
    fun provideSalePointsApi(
        context: Context,
        retrofit: Retrofit
    ): SalePointsApi =
        if (USE_MOCKED_SALE_POINTS_API) {
            MockedSalePointsApi(context)
        } else {
            retrofit.create(SalePointsApi::class.java)
        }

    @Provides
    @Singleton
    fun provideEventsApi(
        context: Context,
        retrofit: Retrofit
    ): EventsApi =
        if (USE_MOCKED_EVENTS_API) {
            MockedEventsApi(context)
        } else {
            retrofit.create(EventsApi::class.java)
        }

    @Provides
    @Singleton
    fun provideTagsApi(
        context: Context,
        retrofit: Retrofit
    ): TagsApi =
        if (USE_MOCKED_TAGS_API) {
            MockedTagsApi(context)
        } else {
            retrofit.create(TagsApi::class.java)
        }

    @Provides
    @Singleton
    fun provideInsulinApi(
        retrofit: Retrofit
    ): DrugNameApi =
        if (USE_MOCKED_INSULIN_DRUG_API) {
            MockedDrugNameApi()
        } else {
            retrofit.create(DrugNameApi::class.java)
        }

    @Provides
    @Singleton
    fun provideFirmwareApi(
        context: Context,
        retrofit: Retrofit
    ): FirmwareApi =
        if (USE_MOCKED_FIRMWARE_API) {
            MockedFirmwareApi(context)
        } else {
            retrofit.create(FirmwareApi::class.java)
        }

    @Provides
    @Singleton
    fun provideFeedbackApi(
        retrofit: Retrofit
    ): FeedbackApi =
        if (USE_MOCKED_FEEDBACK_API) {
            MockedFeedbackApi()
        } else {
            retrofit.create(FeedbackApi::class.java)
        }

    @Provides
    @Singleton
    fun provideReportsApi(
        context: Context,
        retrofit: Retrofit
    ): ReportsApi =
        if (USE_MOCKED_REPORTS_API) {
            MockedReportsApi(context)
        } else {
            retrofit.create(ReportsApi::class.java)
        }

    @Provides
    @Singleton
    fun provideCalculatorApi(
        context: Context,
        retrofit: Retrofit
    ): CalculatorApi =
        if (USE_MOCKED_CALCULATOR_API) {
            CalculatorMockedApi(context)
        } else {
            retrofit.create(CalculatorApi::class.java)
        }

    @Provides
    @Singleton
    fun provideFatSecretApi(
        @FatSecret(FatSecretAnnotationType.Retrofit) retrofit: Retrofit
    ): FatSecretApi = retrofit.create(FatSecretApi::class.java)

    @Provides
    @Singleton
    fun provideFatSecretTokenApi(
        @FatSecret(FatSecretAnnotationType.Token) retrofit: Retrofit
    ): FatSecretTokenApi = retrofit.create(FatSecretTokenApi::class.java)

    @Provides
    @Singleton
    fun providePersonalDataApi(
        retrofit: Retrofit
    ): PersonalDataApi =
        if (USE_MOCKED_PERSONAL_DATA_API) {
            PersonalDataMockedApi()
        } else {
            retrofit.create(PersonalDataApi::class.java)
        }
}
