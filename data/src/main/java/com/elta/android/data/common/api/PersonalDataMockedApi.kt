package com.elta.android.data.common.api

import io.reactivex.Single

class PersonalDataMockedApi : PersonalDataApi {

    override fun getIiotSdkLogin(): Single<String> =
        Single.just("elta-dev")

    override fun getIiotSdkPassword(): Single<String> =
        Single.just("HtXHMyM6yn")
}
