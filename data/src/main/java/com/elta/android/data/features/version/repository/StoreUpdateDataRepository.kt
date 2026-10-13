package com.elta.android.data.features.version.repository

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.elta.android.data.BuildConfig
import com.elta.android.domain.features.remoteconfig.repository.RemoteConfigRepository
import com.elta.android.domain.features.version.model.AppUpdateOffer
import com.elta.android.domain.features.version.repository.StoreUpdateRepository
import javax.inject.Inject

class StoreUpdateDataRepository @Inject constructor(
    private val remoteConfig: RemoteConfigRepository,
    private val preferences: SharedPreferences,
    private val context: Context
) : StoreUpdateRepository {

    override fun availableUpdates(): List<AppUpdateOffer> {
        if (BuildConfig.APP_STORE == "huawei") return emptyList()
        val packageManager = context.packageManager
        val versionCode = try {
            @Suppress("DEPRECATION")
            PackageInfoCompat.getLongVersionCode(packageManager.getPackageInfo(context.packageName, 0))
        } catch (e: PackageManager.NameNotFoundException) {
            return emptyList()
        }
        val installer = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstallerPackageName(context.packageName)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            null
        } catch (e: SecurityException) {
            null
        }
        return remoteConfig.getAppUpdateConfig().availableUpdates(versionCode, installer, context.packageName)
    }

    override fun lastPromptTimeMillis(): Long = preferences.getLong(LAST_PROMPT_TIME, 0)

    override fun recordPromptShown() {
        preferences.edit().putLong(LAST_PROMPT_TIME, System.currentTimeMillis()).apply()
    }

    private companion object {
        const val LAST_PROMPT_TIME = "optional_store_update_prompt_time_ms"
    }
}
