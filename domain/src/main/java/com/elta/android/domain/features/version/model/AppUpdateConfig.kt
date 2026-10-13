package com.elta.android.domain.features.version.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.net.URI
import java.net.URLDecoder
import java.util.Locale

enum class AppUpdateStore(val installerPackage: String) {
    GOOGLE_PLAY("com.android.vending"),
    RUSTORE("ru.vk.store");

    companion object {
        fun fromInstaller(packageName: String?): AppUpdateStore? =
            entries.firstOrNull { it.installerPackage == packageName }
    }
}

@Parcelize
data class AppUpdateOffer(
    val store: AppUpdateStore,
    val versionCode: Long,
    val url: String
) : Parcelable

data class PublishedAppUpdate(val versionCode: Long = 0, val url: String = "")

data class AppUpdateConfig(
    val enabled: Boolean = false,
    val googlePlay: PublishedAppUpdate = PublishedAppUpdate(),
    val ruStore: PublishedAppUpdate = PublishedAppUpdate()
) {
    fun availableUpdates(
        installedVersionCode: Long,
        installerPackage: String?,
        packageName: String
    ): List<AppUpdateOffer> {
        if (!enabled || installedVersionCode !in 1..MAX_VERSION_CODE) return emptyList()
        val installedStore = AppUpdateStore.fromInstaller(installerPackage)
        return listOf(AppUpdateStore.GOOGLE_PLAY to googlePlay, AppUpdateStore.RUSTORE to ruStore)
            .filter { (store, update) ->
                (installedStore == null || installedStore == store) &&
                    update.versionCode in (installedVersionCode + 1)..MAX_VERSION_CODE &&
                    isStoreUrl(store, update.url, packageName)
            }
            .map { (store, update) -> AppUpdateOffer(store, update.versionCode, update.url) }
    }

    private fun isStoreUrl(store: AppUpdateStore, url: String, packageName: String): Boolean =
        runCatching {
            val uri = URI(url)
            if (uri.scheme != "https" || uri.userInfo != null || uri.port != -1) return false
            when (store) {
                AppUpdateStore.GOOGLE_PLAY -> {
                    val query = uri.rawQuery.orEmpty().split('&').map { it.split('=', limit = 2) }
                    uri.host?.lowercase(Locale.ROOT) == "play.google.com" &&
                        uri.path == "/store/apps/details" &&
                        query.count { it.first() == "id" } == 1 &&
                        query.any {
                            it.first() == "id" && it.size == 2 &&
                                URLDecoder.decode(it[1], "UTF-8") == packageName
                        }
                }
                AppUpdateStore.RUSTORE -> {
                    uri.host?.lowercase(Locale.ROOT) in setOf("www.rustore.ru", "rustore.ru") &&
                        uri.path.trimEnd('/') == "/catalog/app/$packageName"
                }
            }
        }.getOrDefault(false)

    private companion object {
        const val MAX_VERSION_CODE = 2_100_000_000L
    }
}
