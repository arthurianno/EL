package com.elta.android.data.features.version.datasource

import com.elta.android.data.features.version.api.VersionApi
import io.reactivex.Single
import com.elta.android.data.features.version.model.AppVersionNetworkRequest
import io.reactivex.Completable
import javax.inject.Inject

class VersionDataSource @Inject constructor(
    private val api: VersionApi
) : VersionSource {
    override fun checkVersion(appId: String, appVersion: String): Single<String> =
        api.checkAppVersion(appId, appVersion)
            .map { it.update }

    override fun sendAppVersion(appVersion: AppVersionNetworkRequest): Completable =
        api.sendAppVersion(appVersion)
}
