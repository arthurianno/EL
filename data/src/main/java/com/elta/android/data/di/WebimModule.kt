package com.elta.android.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.webim.android.sdk.Webim
import ru.webim.android.sdk.WebimSession

private const val ACCOUNT_NAME = "wwwmarslabru"
private const val LOCATION_NAME = "mobile"

@Module
class WebimModule {

    @Provides
    fun provideWebimSession(context: Context): WebimSession = Webim.newSessionBuilder()
        .setAccountName(ACCOUNT_NAME)
        .setLocation(LOCATION_NAME)
        .setContext(context)
        .build()
}
