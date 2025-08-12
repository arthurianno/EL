package com.elta.android.common.di.qualifires


import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NewsApi(val value: NewsApiAnnotationType)

enum class NewsApiAnnotationType {
    BaseUrl,
    OkHttpClient,
    Retrofit,
    Api,
    Interceptors,
    NetworkInterceptors
}