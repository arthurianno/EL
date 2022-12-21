package com.elta.android.data.di

import dagger.Module
import dagger.Provides
import ru.webim.android.sdk.Webim
import ru.webim.android.sdk.WebimSession

private const val ACCOUNT_NAME = "wwwmarslabru"
private const val LOCATION_NAME = "mobile"

@Module
class WebimModule {

    @Provides
    fun provideWebimSession(): WebimSession = Webim.newSessionBuilder()
        .setAccountName(ACCOUNT_NAME)
        .setLocation(LOCATION_NAME)
        .build()
}
