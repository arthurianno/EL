package com.elta.android.data.features.version.datasource

import com.elta.android.data.features.version.model.AppVersionNetworkRequest
import io.reactivex.Completable
import io.reactivex.Single

interface VersionSource {

    fun checkVersion(appId: String, appVersion: String): Single<String>

    fun sendAppVersion(appVersion: AppVersionNetworkRequest): Completable
}
