package com.elta.android.data.features.user.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.data.features.sync.manger.LocalSyncManager
import com.elta.android.data.features.user.datasource.ProfileDataSource
import com.elta.android.data.features.user.dto.ProfileSettingsNetworkResponse
import com.elta.android.data.features.user.mapper.toDomain
import com.elta.android.data.features.user.mapper.toNetwork
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.model.ProfileSettings
import com.elta.android.domain.features.user.repository.ProfileRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class ProfileDataRepository @Inject constructor(
    @Cache private val cachedSource: ProfileDataSource,
    @Remote private val remoteSource: ProfileDataSource,
    private val syncManger: LocalSyncManager
) : ProfileRepository {

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
            run {
                if (isCached) {
                    cachedSource.getUserProfile()
                } else {
                    remoteSource.getUserProfile()
                        .flatMap { cachedSource.getUserProfile() }
                }
            }
                .flatMap { profile ->
                    run {
                        if (isCached) {
                            cachedSource.getProfileSettings()
                        } else {
                            remoteSource.getProfileSettings()
                                .flatMap { cachedSource.getProfileSettings() }
                        }
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
        glucoseFormat: GlucoseFormat?
    ): Completable =
        cachedSource.getProfileSettings()
            .map {
                ProfileSettingsNetworkResponse(
                    isOnboarded = isOnboarded ?: it.isOnboarded,
                    glucoseFormat = glucoseFormat?.toNetwork() ?: it.glucoseFormat
                )
            }
            .flatMap {
                cachedSource.updateProfileSettings(it)
                    .toSingleDefault(it)
            }
            .flatMapCompletable {
                remoteSource.updateProfileSettings(it)
            }
}
