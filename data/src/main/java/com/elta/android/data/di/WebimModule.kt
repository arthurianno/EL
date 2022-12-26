package com.elta.android.data.di

import com.elta.android.common.di.qualifires.Webim
import com.elta.android.common.di.qualifires.WebimAnnotationType
import dagger.Module
import dagger.Provides

private const val ACCOUNT_NAME = "wwwmarslabru"
private const val LOCATION_NAME = "mobile"
private const val PRIVATE_KEY = "8599c5abfcd7342b5feac6599279ca06"

@Module
class WebimModule {

    @Provides
    @Webim(WebimAnnotationType.Account)
    fun provideWebimAccountName(): String = ACCOUNT_NAME

    @Provides
    @Webim(WebimAnnotationType.Location)
    fun provideWebimLocationName(): String = LOCATION_NAME

    @Provides
    @Webim(WebimAnnotationType.PrivateKey)
    fun provideWebimPrivateKey(): String = PRIVATE_KEY
}
