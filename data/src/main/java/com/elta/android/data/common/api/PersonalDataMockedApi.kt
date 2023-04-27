package com.elta.android.data.common.api

import io.reactivex.Single

private enum class WebimData(val accountName: String, val privateKay: String) {
    MarsLab(accountName = "wwwmarslabru", privateKay = "8599c5abfcd7342b5feac6599279ca06"),
    Elta(accountName = "eltaltdru", privateKay = "7d112ff804823419b208678bd779f81f")
}

class PersonalDataMockedApi : PersonalDataApi {

    private val webimData = WebimData.MarsLab
    override fun getIiotSdkLogin(): Single<String> =
        Single.just("elta-dev")

    override fun getIiotSdkPassword(): Single<String> =
        Single.just("HtXHMyM6yn")

    override fun getWebimAccountName(): Single<String> =
        Single.just(webimData.accountName)

    override fun getWebimPrivateKey(): Single<String> =
        Single.just(webimData.privateKay)
}
