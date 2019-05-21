package com.elta.android.data.features.user.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.sync.manger.LocalSyncManager
import com.elta.android.data.features.user.datasource.ProfileDataSource
import com.elta.android.data.features.user.dto.ProfileDto
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.repository.ProfileRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class ProfileDataRepository @Inject constructor(
    private val toDtoMapper: Mapper<Profile, ProfileDto>,
    private val toDomainMapper: Mapper<ProfileDto, Profile>,
    @Cache private val cachedSource: ProfileDataSource,
    @Remote private val remoteSource: ProfileDataSource,
    private val syncManger: LocalSyncManager
) : ProfileRepository {

    override fun updateProfile(profile: Profile): Completable {
        val dto = toDtoMapper.mapFromObject(profile)
        return cachedSource.updateProfile(dto)
            .andThen(
                remoteSource.updateProfile(dto)
                    .onErrorResumeNext {
                        syncManger.saveAsUpdated(profile)
                    }
            )
    }

    override fun getProfile(): Single<Profile> =
        cachedSource.getUserProfile()
            .map(toDomainMapper::mapFromObject)

    override fun sync(): Completable =
        remoteSource.getUserProfile()
            .flatMapCompletable {
                syncManger.needToSync<Profile>()
                    .flatMapCompletable { needToSync ->
                        when (needToSync) {
                            true -> cachedSource.getUserProfile()
                                .flatMapCompletable { profile ->
                                    remoteSource.updateProfile(profile)
                                }
                                .andThen(syncManger.setAllSynced<Profile>())
                            else -> Completable.complete()
                        }
                    }
            }
}