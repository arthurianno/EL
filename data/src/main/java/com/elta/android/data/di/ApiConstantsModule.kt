package com.elta.android.data.di

import com.elta.android.data.core.qualifires.ServerUrl
import dagger.Module
import dagger.Provides

@Module
@Suppress("FunctionOnlyReturningConstant")
class ApiConstantsModule(private val isDebugMode: Boolean) {

    @Provides
    @ServerUrl
    fun provideServerUrl() = when (isDebugMode) {
        true -> SERVER_URL_TEST
        else -> SERVER_URL_PROD
    }

    private companion object {
        const val SERVER_URL_PROD = "https://www.google.com/"
        const val SERVER_URL_TEST = "https://www.google.com/"
    }
}