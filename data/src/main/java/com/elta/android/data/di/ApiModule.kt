package com.elta.android.data.di

import com.elta.android.data.features.feature1.api.TestApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@Suppress("FunctionOnlyReturningConstant", "TooManyFunctions")
class ApiModule {

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): TestApi = retrofit.create<TestApi>(TestApi::class.java)

    object ApiConfig {
        const val USE_MOCKED_AUTO_LOGIN_API = false
        const val USE_MOCKED_AUTH_API = false
        const val USE_MOCKED_AUTH_MNP_API = false
        const val USE_MOCKED_RESOURCES_API = false
        const val USE_MOCKED_TARIFF_API = false
        const val USE_MOCKED_SERVICES_API = false
        const val USE_MOCKED_FINANCE_API = false
        const val USE_MOCKED_EPAY_API = false
        const val USE_MOCKED_NEWS_API = false
        const val USE_MOCKED_DEMO_API = false
        const val USE_MOCKED_AR_API = true
        const val USE_MOCKED_ROAMING_API = false
        const val USE_MOCKED_OFFERS_API = false
        const val USE_MOCKED_MESSAGING_API = false
    }
}