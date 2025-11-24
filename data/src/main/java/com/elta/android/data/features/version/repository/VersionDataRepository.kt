package com.elta.android.data.features.version.repository

import android.util.Log
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
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.math.abs

private const val APP_ID = "elta.android.satellite"
private const val SECOND_IN_THREE_DAYS = SECOND_IN_DAY * 3

class VersionDataRepository @Inject constructor(
    private val source: VersionSource,
    private val holder: VersionHolder
) : VersionRepository {

    override fun sendAppVersion(): Completable {
        val cleanVersion = getCleanVersionName()
        Log.e("Sending appVersion", cleanVersion)
        val appVersionInfo = AppVersionNetworkRequest(
            appId = APP_ID,
            appVersion = cleanVersion
        )
        return source.sendAppVersion(appVersionInfo)
    }


    private fun getCleanVersionName(): String {
        val versionString = BuildConfig.VERSION_NAME
        val majorPattern = Pattern.compile("majorVersion='(\\d+)'")
        val minorPattern = Pattern.compile("minorVersion='(\\d+)'")
        val hotfixPattern = Pattern.compile("hotfixVersion='(\\d+)'")

        val majorMatcher = majorPattern.matcher(versionString)
        val minorMatcher = minorPattern.matcher(versionString)
        val hotfixMatcher = hotfixPattern.matcher(versionString)

        return if (majorMatcher.find() && minorMatcher.find() && hotfixMatcher.find()) {
            "${majorMatcher.group(1)}.${minorMatcher.group(1)}.${hotfixMatcher.group(1)}"
        } else {
            Log.e("VersionError", "Failed to parse VERSION_NAME: $versionString")
            versionString
        }
    }

    override fun checkAppVersion(): Single<VersionStatus> {
        val cleanVersion = getCleanVersionName()
        return source.checkVersion(
            appId = APP_ID,
            appVersion = cleanVersion,
            appStore = BuildConfig.APP_STORE
        )
            .map { VersionStatus.valueOf(it) }
            .map { versionStatus -> checkOptionalUpdate(versionStatus) }
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
