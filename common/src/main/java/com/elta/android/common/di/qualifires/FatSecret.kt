package com.elta.android.common.di.qualifires

import javax.inject.Qualifier

@MustBeDocumented
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class FatSecret(val type: FeatSecretAnnotationType)

enum class FeatSecretAnnotationType {
    Retrofit,
    BaseUrl,
    TokenUrl,
    Token,
    ClientId,
    ClientSecret,
    OkHttpClient,
    Interceptors,
    NetworkInterceptors
}
