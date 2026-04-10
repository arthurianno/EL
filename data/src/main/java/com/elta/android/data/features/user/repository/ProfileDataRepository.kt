package com.elta.android.data.features.user.repository

import android.content.Context
import android.util.Log
import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.data.features.sync.manger.LocalSyncManager
import com.elta.android.data.features.user.datasource.ProfileDataSource
import com.elta.android.data.features.user.dto.ProfileSettingsNetworkResponse
import com.elta.android.data.features.user.dto.SupportedLanguageTag
import com.elta.android.data.features.user.mapper.toDomain
import com.elta.android.data.features.user.mapper.toNetwork
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.model.ProfileSettings
import com.elta.android.domain.features.user.repository.ProfileRepository
import io.reactivex.Completable
import io.reactivex.Single
import java.util.Locale
import javax.inject.Inject

class ProfileDataRepository @Inject constructor(
    @Cache private val cachedSource: ProfileDataSource,
    @Remote private val remoteSource: ProfileDataSource,
    private val syncManger: LocalSyncManager,
    private val context: Context
) : ProfileRepository {

    companion object {
        private const val TAG = "LangFlow"
        private const val LANGUAGE_PREFS_NAME = "language_preference"
        private const val LANGUAGE_KEY_SELECTED = "selected_language"
        private const val REGION_KEY_SELECTED = "selected_region"
        private const val DEFAULT_COUNTRY_CODE = "RU"
    }

    override fun updateProfile(profile: Profile): Completable {
        val dto = profile.toNetwork()
        return cachedSource.updateProfile(dto)
            .andThen(
                remoteSource.updateProfile(dto)
                    .onErrorResumeNext {
                        syncManger.saveAsUpdated(profile)
                    }
            )
    }

    override fun getProfile(): Single<Profile> =
        cachedSource.hasProfile().flatMap { isCached ->
            if (isCached) {
                cachedSource.getUserProfile()
            } else {
                remoteSource.getUserProfile()
                    .flatMap { cachedSource.getUserProfile() }
            }
                .flatMap { profile ->
                    if (isCached) {
                        cachedSource.getProfileSettings()
                    } else {
                        remoteSource.getProfileSettings()
                            .flatMap { cachedSource.getProfileSettings() }
                    }
                        .map { profile.toDomain(it.glucoseFormat) }
                }
        }

    override fun getProfileSettings(fromCache: Boolean): Single<ProfileSettings> =
        run {
            if (fromCache) {
                cachedSource.getProfileSettings()
                    .onErrorResumeNext {
                        remoteSource.getProfileSettings()
                            .flatMap {
                                cachedSource.updateProfileSettings(it)
                                    .toSingleDefault(it)
                            }
                    }
            } else {
                remoteSource.getProfileSettings()
                    .flatMap {
                        cachedSource.updateProfileSettings(it)
                            .toSingleDefault(it)
                    }
            }
        }.map { it.toDomain() }

    override fun getUserId(): Single<String> =
        getProfile().map(Profile::email)

    override fun sync(): Completable =
        remoteSource.getProfileSettings()
            .flatMap {
                remoteSource.getUserProfile()
            }
            .flatMapCompletable {
                syncManger.needToSync<Profile>()
                    .flatMapCompletable { needToSync ->
                        if (needToSync) {
                            cachedSource.getUserProfile()
                                .flatMapCompletable { profile ->
                                    remoteSource.updateProfile(profile)
                                }
                                .andThen(syncManger.setAllSynced<Profile>())
                        } else {
                            Completable.complete()
                        }
                    }
            }

    override fun updateProfileSettings(
        isOnboarded: Boolean?,
        glucoseFormat: GlucoseFormat
    ): Completable = cachedSource.getProfileSettings()
        .onErrorReturn {
            ProfileSettingsNetworkResponse(
                isOnboarded = true,
                glucoseFormat = glucoseFormat.toNetwork(),
                languageTag = resolveLanguageTag(null)
            )
        }
        .flatMapCompletable { cachedSettings ->
            val resolvedTag = resolveLanguageTag(cachedSettings.languageTag)
            val response = ProfileSettingsNetworkResponse(
                isOnboarded = isOnboarded ?: cachedSettings.isOnboarded,
                glucoseFormat = glucoseFormat.toNetwork(),
                languageTag = resolvedTag
            )
            Log.i(TAG, "updateProfileSettings: sending languageTag=$resolvedTag, countryCode=${response.countryCode}, isOnboarded=${response.isOnboarded}")

            cachedSource.updateProfileSettings(response)
                .andThen(
                    remoteSource.updateProfileSettings(response)
                        .doOnComplete { Log.i(TAG, "updateProfileSettings: remote success languageTag=$resolvedTag, countryCode=${response.countryCode}") }
                        .doOnError { e -> Log.e(TAG, "updateProfileSettings: remote error languageTag=$resolvedTag, countryCode=${response.countryCode}, msg=${e.message}") }
                        .onErrorComplete()
                )
        }

    override fun updateLanguageTag(languageTag: String): Completable =
        getProfileSettingsForUpdate()
            .flatMapCompletable { currentSettings ->
                val resolvedTag = resolveLanguageTag(languageTag)
                val resolvedCountry = resolveCountryCode(currentSettings.countryCode)
                val response = currentSettings.copy(
                    languageTag = resolvedTag,
                    countryCode = resolvedCountry
                )

                Log.i(TAG, "updateLanguageTag: sending languageTag=$resolvedTag (input=$languageTag), countryCode=$resolvedCountry (prev=${currentSettings.countryCode})")

                cachedSource.updateProfileSettings(response)
                    .andThen(
                        remoteSource.updateProfileSettings(response)
                            .doOnComplete { Log.i(TAG, "updateLanguageTag: remote success languageTag=$resolvedTag, countryCode=$resolvedCountry") }
                            .doOnError { e -> Log.e(TAG, "updateLanguageTag: remote error languageTag=$resolvedTag, countryCode=$resolvedCountry, msg=${e.message}") }
                            .onErrorComplete()
                    )
            }

    private fun getProfileSettingsForUpdate(): Single<ProfileSettingsNetworkResponse> =
        cachedSource.getProfileSettings()
            .onErrorResumeNext {
                remoteSource.getProfileSettings()
                    .flatMap { remoteSettings ->
                        cachedSource.updateProfileSettings(remoteSettings)
                            .toSingleDefault(remoteSettings)
                    }
            }

    private fun resolveLanguageTag(languageTag: String? = null): String {
        val selectedLanguage = context
            .getSharedPreferences(LANGUAGE_PREFS_NAME, Context.MODE_PRIVATE)
            .getString(LANGUAGE_KEY_SELECTED, null)
        val rawLanguage = languageTag ?: selectedLanguage ?: Locale.getDefault().language
        val resolved = SupportedLanguageTag.fromRawValue(rawLanguage).value
        Log.i(TAG, "resolveLanguageTag(input=$languageTag, savedInPrefs=$selectedLanguage, systemDefault=${Locale.getDefault().language}) → resolved=$resolved")
        return resolved
    }

    /**
     * Читает сохранённый код страны из SharedPreferences (тот же преф-файл, что у LocaleHelper).
     * Если код уже передан в [existingCode] — возвращает его (не перезаписываем то, что пришло с сервера).
     * По умолчанию — "RU".
     */
    private fun resolveCountryCode(existingCode: String? = null): String {
        if (existingCode != null) {
            Log.i(TAG, "resolveCountryCode(existingCode=$existingCode) → kept as-is")
            return existingCode
        }
        val fromPrefs = context
            .getSharedPreferences(LANGUAGE_PREFS_NAME, Context.MODE_PRIVATE)
            .getString(REGION_KEY_SELECTED, DEFAULT_COUNTRY_CODE)
            ?: DEFAULT_COUNTRY_CODE
        Log.i(TAG, "resolveCountryCode(existingCode=null, savedInPrefs=$fromPrefs) → resolved=$fromPrefs")
        return fromPrefs
    }
}
