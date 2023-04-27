package com.elta.android.data.common.api

import io.reactivex.Single

interface PersonalDataApi {

    fun getIiotSdkLogin(): Single<String>
    fun getIiotSdkPassword(): Single<String>
    fun getWebimAccountName(): Single<String>
    fun getWebimPrivateKey(): Single<String>
}
