package com.elta.android.data.di

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
class CoroutineModule {

    @Provides
    fun provideCoroutineDispatcher(): CoroutineDispatcher =
        Dispatchers.IO
}
