package com.elta.android.data.features.user.mapper // ktlint-disable filename

import com.elta.android.data.features.user.cache.dto.ProfileSettingsDbEntity
import com.elta.android.data.features.user.dto.DiabeteTypeNetworkEntity
import com.elta.android.data.features.user.dto.GenderTypeNetworkEntity
import com.elta.android.data.features.user.dto.GlucoseFormatNetworkEntity
import com.elta.android.data.features.user.dto.GlucoseLevelNetworkEntity
import com.elta.android.data.features.user.dto.HealthAppNetworkEntity
import com.elta.android.data.features.user.dto.HealthAppTypeNetworkEntity
import com.elta.android.data.features.user.dto.PersonNetworkEntity
import com.elta.android.data.features.user.dto.ProfileNetworkResponse
import com.elta.android.data.features.user.dto.ProfileSettingsNetworkResponse
import com.elta.android.data.features.user.dto.SocialNetworkDto
import com.elta.android.data.features.user.dto.SocialNetworkTypeNetworkEntity
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.domain.features.user.model.Gender
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.user.model.HealthApp
import com.elta.android.domain.features.user.model.HealthAppType
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.model.ProfileSettings
import com.elta.android.domain.features.user.model.SocialNetwork
import com.elta.android.domain.features.user.model.SocialNetworkType

internal fun ProfileSettingsNetworkResponse.toDb(id: Long): ProfileSettingsDbEntity =
    ProfileSettingsDbEntity(
        id = id,
        isOnboarded = isOnboarded,
        glucoseFormat = glucoseFormat.name
    )

internal fun ProfileSettingsDbEntity.toNetwork(): ProfileSettingsNetworkResponse =
    ProfileSettingsNetworkResponse(
        isOnboarded = isOnboarded,
        glucoseFormat = GlucoseFormatNetworkEntity.valueOf(glucoseFormat)
    )

internal fun ProfileSettingsNetworkResponse.toDomain(): ProfileSettings =
    ProfileSettings(
        isOnboarded = isOnboarded,
        glucoseFormat = glucoseFormat.toDomain()
    )

internal fun Profile.toNetwork(): ProfileNetworkResponse =
    ProfileNetworkResponse(
        diabetes = diabetes?.let { DiabeteTypeNetworkEntity.valueOf(it.name) },
        weight = weight,
        gender = gender.toNetwork(),
        person = if (firstName == null && secondName == null) {
            null
        } else {
            PersonNetworkEntity(
                firstName = firstName,
                lastName = secondName
            )
        },
        glucoseLevelsAverage = GlucoseLevelNetworkEntity(
            minValue = glucoseLevelSettings.normal.start,
            maxValue = glucoseLevelSettings.normal.end
        ),
        glucoseLevelsBeforeEating = GlucoseLevelNetworkEntity(
            minValue = glucoseLevelBeforeEatSettings.normal.start,
            maxValue = glucoseLevelBeforeEatSettings.normal.end
        ),
        glucoseLevelsAfterEating = GlucoseLevelNetworkEntity(
            minValue = glucoseLevelAfterEatSettings.normal.start,
            maxValue = glucoseLevelAfterEatSettings.normal.end
        ),
        email = email,
        socialNetworks = null,
        healthApps = healthApps?.map { it.toNetwork() },
        timeStamp = timeStamp
    )

internal fun ProfileNetworkResponse.toDomain(glucoseFormat: GlucoseFormatNetworkEntity): Profile =
    Profile(
        firstName = person?.firstName,
        secondName = person?.lastName,
        gender = gender.toDomain(),
        email = email,
        glucoseLevelSettings = glucoseLevelsAverage.toSettings(),
        glucoseLevelBeforeEatSettings = glucoseLevelsBeforeEating.toSettings(),
        glucoseLevelAfterEatSettings = glucoseLevelsAfterEating.toSettings(),
        glucoseFormat = glucoseFormat.toDomain(),
        diabetes = diabetes?.toDomain(),
        weight = weight,
        socialNetworks = socialNetworks?.map { it.toDomain() },
        healthApps = healthApps?.map { it.toDomain() },
        timeStamp = timeStamp

    )

private fun GlucoseFormatNetworkEntity.toDomain(): GlucoseFormat =
    when (this) {
        GlucoseFormatNetworkEntity.CAPILLARY -> GlucoseFormat.CAPILLARY
        GlucoseFormatNetworkEntity.PLASMA -> GlucoseFormat.PLASMA
    }

internal fun GlucoseFormat.toNetwork(): GlucoseFormatNetworkEntity =
    when (this) {
        GlucoseFormat.CAPILLARY -> GlucoseFormatNetworkEntity.CAPILLARY
        GlucoseFormat.PLASMA -> GlucoseFormatNetworkEntity.PLASMA
    }

internal fun HealthApp.toNetwork(): HealthAppNetworkEntity =
    HealthAppNetworkEntity(
        type = HealthAppTypeNetworkEntity.valueOf(type.name),
        isActive = isActive
    )

private fun Gender.toNetwork(): GenderTypeNetworkEntity? =
    when (this) {
        Gender.MALE -> GenderTypeNetworkEntity.MALE
        Gender.FEMALE -> GenderTypeNetworkEntity.FEMALE
        Gender.NOT_SPECIFIED -> null
    }

private fun GenderTypeNetworkEntity?.toDomain(): Gender =
    when (this) {
        GenderTypeNetworkEntity.MALE -> Gender.MALE
        GenderTypeNetworkEntity.FEMALE -> Gender.FEMALE
        else -> Gender.NOT_SPECIFIED
    }

private fun GlucoseLevelNetworkEntity?.toSettings(): GlucoseLevelSettings {
    return if (this == null || this.minValue == null || this.maxValue == null) {
        GlucoseLevelSettings()
    } else {
        GlucoseLevelSettings.fromNormalValues(this.minValue, this.maxValue)
    }
}

private fun DiabeteTypeNetworkEntity.toDomain(): Diabetes =
    when (this) {
        DiabeteTypeNetworkEntity.FIRST -> Diabetes.FIRST
        DiabeteTypeNetworkEntity.SECOND -> Diabetes.SECOND
        DiabeteTypeNetworkEntity.LADA -> Diabetes.LADA
        DiabeteTypeNetworkEntity.GESTATIONAL -> Diabetes.GESTATIONAL
        DiabeteTypeNetworkEntity.PREDIABETES -> Diabetes.PREDIABETES
        DiabeteTypeNetworkEntity.OTHER -> Diabetes.OTHER
    }

private fun HealthAppNetworkEntity.toDomain(): HealthApp =
    HealthApp(
        type = when (type) {
            HealthAppTypeNetworkEntity.GOOGLE_FIT -> HealthAppType.GOOGLE_FIT
            HealthAppTypeNetworkEntity.APPLE_HEALTH -> HealthAppType.APPLE_HEALTH
        },
        isActive = isActive
    )

private fun SocialNetworkDto.toDomain(): SocialNetwork =
    SocialNetwork(
        type = when (type) {
            SocialNetworkTypeNetworkEntity.FB -> SocialNetworkType.FB
            SocialNetworkTypeNetworkEntity.VK -> SocialNetworkType.VK
            SocialNetworkTypeNetworkEntity.OK -> SocialNetworkType.OK
        },
        isLinked = isLinked
    )
