package com.elta.android.data.features.user.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.common.onConnectionErrorCompletes
import com.elta.android.data.common.onConnectionErrorResumeDefault
import com.elta.android.data.features.user.datasource.ProfileDataSource
import com.elta.android.data.features.user.dto.ProfileDto
import com.elta.android.data.features.user.storage.OnboardingStorage
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
    private val onboardingStorage: OnboardingStorage
) : ProfileRepository {

    override fun updateProfile(profile: Profile): Completable {
        val dto = toDtoMapper.mapFromObject(profile)
        return cachedSource.updateProfile(dto)
            .andThen(
                remoteSource.updateProfile(dto)
                    .onConnectionErrorCompletes()
            )
            .andThen(
                Completable.fromAction { onboardingStorage.isOnboardingPassed = true }
            )
    }

    override fun getProfile(): Single<Profile> =
        remoteSource.getUserProfile()
            .onConnectionErrorResumeDefault { cachedSource.getUserProfile() }
            .flatMap { cachedSource.getUserProfile() }
            .map(toDomainMapper::mapFromObject)

    override fun isOnboardingPassed(): Single<Boolean> =
        Single.just(onboardingStorage.isOnboardingPassed)
}