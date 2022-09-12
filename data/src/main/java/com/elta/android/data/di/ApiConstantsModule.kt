package com.elta.android.data.di

import com.elta.android.data.core.qualifires.ServerUrl
import dagger.Module
import dagger.Provides

@Module
@Suppress("FunctionOnlyReturningConstant")
class ApiConstantsModule(private val serverUrl: String) {

    @Provides
    @ServerUrl
    fun provideServerUrl() = serverUrl
}
