package com.elta.android.data.di

import android.content.SharedPreferences
import com.elta.android.data.core.qualifires.ServerUrl
import com.elta.android.domain.features.appsettings.model.BackendVariant
import com.elta.android.domain.features.appsettings.model.BackendVariant.Companion.toBackendVariantName
import com.nullgr.core.preferences.get
import com.nullgr.core.preferences.set
import dagger.Module
import dagger.Provides

@Module
@Suppress("FunctionOnlyReturningConstant")
class ApiConstantsModule(
    private val pref: SharedPreferences,
    private val serverUrl: String,
    private val isDebug: Boolean
) {

    @Provides
    @ServerUrl
    fun provideServerUrl(): String =
        if (isDebug) {
            val backendVariant: String? = pref[BackendVariant.NAME_BACKEND_VARIANT]
            if (!backendVariant.isNullOrBlank()) {
                BackendVariant.valueOf(backendVariant).url
            } else {
                pref[BackendVariant.NAME_BACKEND_VARIANT] = serverUrl.toBackendVariantName()
                serverUrl
            }
        } else serverUrl
}

