package com.elta.android.data.common.datasource

import com.elta.android.data.common.api.PersonalDataApi
import com.elta.android.data.features.consultant.model.WebimAccount
import com.nullgr.core.security.prefs.CryptoPreferences
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.rx2.asFlow
import javax.inject.Inject

private const val IIOT_SDK_LOGIN_KEY = "SDK_login_key"
private const val IIOT_SDK_PASSWORD_KEY = "SDK_password_key"
private const val WEBIM_ACCOUNT_NAME = "webim_account_name"
private const val WEBIM_PRIVATE_KEY = "webim_private_key"

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

    fun getWebimAccount(): Flow<WebimAccount> = flow {
        preferences.getString(WEBIM_ACCOUNT_NAME, null)
            .flowOnGetAndCache(
                cachedFun = { preferences.setString(WEBIM_ACCOUNT_NAME, it) },
                getFun = {
                    api.getWebimAccountName()
                        .toObservable()
                        .asFlow()
                }
            )
            .combineTransform(
                preferences.getString(WEBIM_PRIVATE_KEY, null)
                    .flowOnGetAndCache(
                        cachedFun = { preferences.setString(WEBIM_PRIVATE_KEY, it) },
                        getFun = {
                            api.getWebimPrivateKey()
                                .toObservable()
                                .asFlow()
                        }
                    )
            ) { name, key ->
                emit(WebimAccount(name, key))
            }
    }
}

private fun <T> T?.flowOnGetAndCache(
    cachedFun: (T) -> Unit,
    getFun: () -> Flow<T>
): Flow<T> = flow {
    this@flowOnGetAndCache?.let { emit(it) }
        ?: emitAll(run { getFun().onEach { cachedFun(it) } })
}
