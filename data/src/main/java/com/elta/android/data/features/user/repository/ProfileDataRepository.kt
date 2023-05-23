package com.elta.android.data.features.user.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.data.features.sync.manger.LocalSyncManager
import com.elta.android.data.features.user.datasource.ProfileDataSource
import com.elta.android.data.features.user.dto.ProfileSettingsNetworkRequest
import com.elta.android.data.features.user.mapper.toDomain
import com.elta.android.data.features.user.mapper.toNetwork
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.user.model.Profile
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
                        .map { profile to it }
                }
        }
            .map { it.first.toDomain(glucoseFormat = it.second.glucoseFormat) }

    override fun getUserId(): Single<String> =
        getProfile().map(Profile::email)

    override fun sync(): Completable =
        remoteSource.getUserProfile()
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
            .flatMapCompletable { settings ->
                cachedSource.updateProfileSettings(
                    ProfileSettingsNetworkRequest(
                        isOnboarded = isOnboarded ?: settings.isOnboarded,
                        glucoseFormat = glucoseFormat?.toNetwork() ?: settings.glucoseFormat
                    )
                )
            }
}
