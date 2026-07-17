package com.elta.android.data.common.api

import io.reactivex.Single

interface PersonalDataApi {

    fun getIiotSdkLogin(): Single<String>
    fun getIiotSdkPassword(): Single<String>
}
