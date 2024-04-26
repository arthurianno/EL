package com.elta.android.data.features.version.repository

import com.elta.android.common.utils.SECOND_IN_DAY
import com.elta.android.common.utils.timestamp
import com.elta.android.data.BuildConfig
import com.elta.android.data.features.common.storage.VersionHolder
import com.elta.android.data.features.version.datasource.VersionSource
import com.elta.android.data.features.version.model.AppVersionNetworkRequest
import com.elta.android.domain.features.version.model.VersionStatus
import com.elta.android.domain.features.version.repository.VersionRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import kotlin.math.abs

private const val APP_ID = "elta.android.satellite"
private const val SECOND_IN_THREE_DAYS = SECOND_IN_DAY * 3

class VersionDataRepository @Inject constructor(
    private val source: VersionSource,
    private val holder: VersionHolder
) : VersionRepository {

    override fun sendAppVersion(): Completable {
        val appVersionInfo = AppVersionNetworkRequest(
            appId = APP_ID,
            appVersion = BuildConfig.VERSION_NAME
        )

        return source.sendAppVersion(appVersionInfo)
    }

    override fun checkAppVersion(): Single<VersionStatus> {
        return source.checkVersion(
            appId = APP_ID,
            appVersion = BuildConfig.VERSION_NAME,
            appStore = BuildConfig.APP_STORE
        )
            .map { VersionStatus.valueOf(it) }
            .map { versionStatus ->
                checkOptionalUpdate(versionStatus)
            }
    }

    private fun checkOptionalUpdate(versionStatus: VersionStatus) =
        when (versionStatus) {
            VersionStatus.OPTIONAL -> {
                holder.lastOptionalUpdateSync?.let {
                    val secondSinceLastResponse = abs(it - timestamp())

                    if (secondSinceLastResponse < SECOND_IN_THREE_DAYS && it != 0L) VersionStatus.NEEDLESS
                    else {
                        holder.lastOptionalUpdateSync = timestamp()
                        versionStatus
                    }
                } ?: versionStatus
            }

            else -> versionStatus
        }
}
