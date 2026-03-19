package com.elta.android.data.features.user.repository

import android.content.Context
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
        private const val LANGUAGE_PREFS_NAME = "language_preference"
        private const val LANGUAGE_KEY_SELECTED = "selected_language"
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
                languageTag = resolveLanguageTag()
            )
        }
        .flatMapCompletable { cachedSettings ->
            val response = ProfileSettingsNetworkResponse(
                isOnboarded = isOnboarded ?: cachedSettings.isOnboarded,
                glucoseFormat = glucoseFormat.toNetwork(),
                languageTag = resolveLanguageTag(cachedSettings.languageTag)
            )
            cachedSource.updateProfileSettings(response)
                .andThen(remoteSource.updateProfileSettings(response).onErrorComplete())
        }

    override fun updateLanguageTag(languageTag: String): Completable =
        getProfileSettingsForUpdate()
            .flatMapCompletable { currentSettings ->
                val response = currentSettings.copy(
                    languageTag = resolveLanguageTag(languageTag)
                )
                cachedSource.updateProfileSettings(response)
                    .andThen(remoteSource.updateProfileSettings(response).onErrorComplete())
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
        return SupportedLanguageTag.fromRawValue(rawLanguage).value
    }
}
