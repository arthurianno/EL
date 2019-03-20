package com.elta.android.data.features.user.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.user.cache.SettingsCache
import com.elta.android.data.features.user.cache.dto.SettingsCacheDto
import com.elta.android.data.features.user.dto.ProfileDto
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class SettingsCachedDataSource @Inject constructor(
    private val fromCacheMapper: Mapper<SettingsCacheDto, ProfileDto>,
    private val cache: SettingsCache
) : SettingsDataSource {

    override fun updateUserProfile(gender: String?, weight: Double?, diabetes: String?): Completable =
        Completable.fromCallable {
            val profiles = cache.get(CommonConditions.Empty)
            if (profiles.isNotEmpty()) {
                val profile = profiles[0]
                val newGender = gender ?: profile.gender
                val newWeight = weight ?: profile.weight
                val newDiabetes = diabetes ?: profile.diabetType
                val newProfile = profile.copy(gender = newGender, weight = newWeight, diabetType = newDiabetes)
                cache.update(listOf(newProfile))
            }
        }

    override fun getUserProfile(): Single<ProfileDto> =
        Single.fromCallable {
            val profiles = cache.get(CommonConditions.Empty)
            if (profiles.isNotEmpty()) {
                profiles[0]
            } else {
                throw NoSuchElementException("Current user profile is empty.")
            }
        }.map(fromCacheMapper::mapFromObject)
}