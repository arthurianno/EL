package com.elta.android.data.features.user.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.user.cache.dto.SettingsCacheDto
import com.elta.android.data.features.user.dto.ProfileDto
import javax.inject.Inject

class SettingsToCacheMapper @Inject constructor(
    private val userHolder: UserHolder
) : Mapper<ProfileDto, SettingsCacheDto> {
    override fun mapFromObject(source: ProfileDto): SettingsCacheDto =
        with(source) {
            SettingsCacheDto(
                id = userHolder.currentUser ?: 0,
                diabetType = diabetType.name,
                weight = weight,
                gender = gender.name,
                email = email,
                timeStamp = timeStamp,
                firstName = person?.firstName,
                lastName = person?.lastName,
                minValue = glucoseLevel?.minValue,
                maxValue = glucoseLevel?.maxValue
            )
        }
}