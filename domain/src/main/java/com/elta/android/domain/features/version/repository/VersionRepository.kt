package com.elta.android.domain.features.version.repository

import com.elta.android.domain.features.version.model.VersionStatus
import io.reactivex.Completable
import io.reactivex.Single

interface VersionRepository {

    fun checkAppVersion(): Single<VersionStatus>

    fun sendAppVersion(): Completable
}
