package com.elta.android.data.features.user.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.common.errors.NetworkConnectionError
import com.elta.android.common.mapper.Mapper
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
    @Remote private val remoteSource: ProfileDataSource
) : ProfileRepository {

    override fun updateProfile(profile: Profile): Completable {
        val dto = toDtoMapper.mapFromObject(profile)
        return cachedSource.updateProfile(dto)
            .andThen(
                remoteSource.updateProfile(dto)
                    .onErrorComplete { error -> error is NetworkConnectionError }
            )
    }

    override fun getProfile(): Single<Profile> =
        remoteSource.getUserProfile()
            .onErrorResumeNext { error ->
                when (error) {
                    is NetworkConnectionError -> cachedSource.getUserProfile()
                    else -> Single.error(error)
                }
            }
            .flatMap { cachedSource.getUserProfile() }
            .map(toDomainMapper::mapFromObject)
}