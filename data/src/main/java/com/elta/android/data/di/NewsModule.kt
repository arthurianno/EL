package com.elta.android.data.di

import com.elta.android.common.di.qualifires.NewsApi
import com.elta.android.common.di.qualifires.NewsApiAnnotationType
import com.elta.android.data.common.ConnectivityInterceptor
import com.elta.android.data.core.network.OkHttpClientFactory
import com.elta.android.data.core.network.RetrofitFactory
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.newsChannel.cache.NewsCache
import com.elta.android.data.features.newsChannel.datasource.NewsDataSource
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.File
import javax.inject.Singleton

private const val BASE_URL = "https://test.vdiabete.com/api/news"
@Module
class NewsModule {

    @Provides
    @Singleton
    @NewsApi(NewsApiAnnotationType.BaseUrl)
    fun provideNewsBaseUrl(): String = BASE_URL

    @Provides
    @Singleton
    fun provideNewsCache(
        boxStoreFactory: BoxStoreFactory
    ): NewsCache = NewsCache(boxStoreFactory)

    @Provides
    @Singleton
    @NewsApi(NewsApiAnnotationType.OkHttpClient)
    fun provideNewsOkHttpClient(
        cacheFolder: File,
        @NewsApi(NewsApiAnnotationType.Interceptors) interceptors: List<Interceptor>,
        @NewsApi(NewsApiAnnotationType.NetworkInterceptors) networkInterceptors: List<Interceptor>
    ): OkHttpClient = OkHttpClientFactory.create(
        cacheFolder,
        interceptors,
        networkInterceptors
    )

    @Provides
    @Singleton
    @NewsApi(NewsApiAnnotationType.Retrofit)
    fun provideNewsRetrofit(
        @NewsApi(NewsApiAnnotationType.OkHttpClient) okHttpClient: OkHttpClient,
        callAdapterFactory: CallAdapter.Factory,
        converterFactory: Converter.Factory,
        @NewsApi(NewsApiAnnotationType.BaseUrl) baseUrl: String
    ): Retrofit = RetrofitFactory.create(
        okHttpClient,
        callAdapterFactory,
        converterFactory,
        baseUrl
    )

    @Provides
    @Singleton
    @NewsApi(NewsApiAnnotationType.Api)
    fun provideNewsApi(
        @NewsApi(NewsApiAnnotationType.Retrofit) retrofit: Retrofit
    ): NewsApi = retrofit.create(NewsApi::class.java)

    @Provides
    @Singleton
    @NewsApi(NewsApiAnnotationType.Interceptors)
    fun provideNewsInterceptors(
        connectivityInterceptor: ConnectivityInterceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): List<@JvmWildcard Interceptor> = listOf(
        connectivityInterceptor,
        httpLoggingInterceptor
    )

    @Provides
    @Singleton
    @NewsApi(NewsApiAnnotationType.NetworkInterceptors)
    fun provideNewsNetworkInterceptors(): List<@JvmWildcard Interceptor> = emptyList()


}