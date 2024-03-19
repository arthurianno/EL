package com.elta.android.data.features.version.api

import com.elta.android.data.features.version.model.AppVersionNetworkRequest
import com.elta.android.data.features.version.model.VersionResponse
import io.reactivex.Completable
import io.reactivex.Single

class MockedVersionApi : VersionApi {
    override fun checkAppVersion(appId: String, appVersion: String): Single<VersionResponse> {
        return Single.just(
            VersionResponse(
//                "NEEDLESS"
                        "OPTIONAL"
//                        "MANDATORY"
            )
        )
    }

    override fun sendAppVersion(request: AppVersionNetworkRequest): Completable {
        return Completable.complete()
    }
}
