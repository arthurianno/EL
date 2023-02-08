package com.elta.android.data.di

import com.elta.android.common.di.qualifires.WebimAnnotation
import com.elta.android.common.di.qualifires.WebimAnnotationType
import dagger.Module
import dagger.Provides

private const val ACCOUNT_NAME = "eltaltdru"
private const val LOCATION_NAME = "mobile"
private const val PRIVATE_KEY = "7d112ff804823419b208678bd779f81f"

@Module
class WebimModule {

    @Provides
    @WebimAnnotation(WebimAnnotationType.Account)
    fun provideWebimAccountName(): String = ACCOUNT_NAME

    @Provides
    @WebimAnnotation(WebimAnnotationType.Location)
    fun provideWebimLocationName(): String = LOCATION_NAME

    @Provides
    @WebimAnnotation(WebimAnnotationType.PrivateKey)
    fun provideWebimPrivateKey(): String = PRIVATE_KEY
}
