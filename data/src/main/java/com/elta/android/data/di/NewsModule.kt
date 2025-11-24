package com.elta.android.data.di
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.newsChannel.cache.NewsCache
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NewsModule {
    @Provides
    @Singleton
    fun provideNewsCache(
        boxStoreFactory: BoxStoreFactory
    ): NewsCache = NewsCache(boxStoreFactory)
}