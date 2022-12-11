package com.elta.android.common.di.qualifires

import javax.inject.Qualifier

@MustBeDocumented
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class FatSecret(val type: FatSecretAnnotationType)

enum class FatSecretAnnotationType {
    Retrofit,
    BaseUrl,
    TokenUrl,
    Token,
    ClientId,
    ClientSecret,
    ConsumerKey,
    ConsumerSecret,
    OkHttpClient,
    Interceptors,
    NetworkInterceptors,
    IsOAuth2
}
