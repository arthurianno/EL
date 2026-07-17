package com.elta.android.data.common.datasource

import com.elta.android.data.common.api.PersonalDataApi
import com.nullgr.core.security.prefs.CryptoPreferences
import io.reactivex.Single
import javax.inject.Inject

private const val IIOT_SDK_LOGIN_KEY = "SDK_login_key"
private const val IIOT_SDK_PASSWORD_KEY = "SDK_password_key"

class PersonalDataStorage @Inject constructor(
    private val preferences: CryptoPreferences,
    private val api: PersonalDataApi
) {

    fun getIiotLogin(): Single<String> =
        preferences.getString(IIOT_SDK_LOGIN_KEY, null)?.let {
            Single.just(it)
        }
            ?: api.getIiotSdkLogin()
                .doOnSuccess {
                    preferences.setString(IIOT_SDK_LOGIN_KEY, it)
                }

    fun getIiotPassword(): Single<String> =
        preferences.getString(IIOT_SDK_PASSWORD_KEY, null)?.let {
            Single.just(it)
        }
            ?: api.getIiotSdkPassword()
                .doOnSuccess {
                    preferences.setString(IIOT_SDK_PASSWORD_KEY, it)
                }
}
