package com.elta.android.domain.features.user.model

import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings

data class Profile(
    val firstName: String? = null,
    val secondName: String? = null,
    val gender: Gender? = null,
    val email: String? = null,
    val glucoseLevelSettings: GlucoseLevelSettings = GlucoseLevelSettings.fromNormalValues(
        GlucoseLevelSettings.NORMAL_START,
        GlucoseLevelSettings.NORMAL_END
    ),
    val glucoseLevelBeforeEatSettings: GlucoseLevelSettings = GlucoseLevelSettings.fromNormalValues(
        GlucoseLevelSettings.NORMAL_START,
        GlucoseLevelSettings.NORMAL_END
    ),
    val glucoseLevelAfterEatSettings: GlucoseLevelSettings = GlucoseLevelSettings.fromNormalValues(
        GlucoseLevelSettings.NORMAL_START,
        GlucoseLevelSettings.NORMAL_END
    ),
    val diabetes: Diabetes? = null,
    val weight: Double? = null,
    val hba1cLevel: Double? = null,
    val socialNetworks: List<SocialNetwork>? = null,
    val healthApps: List<HealthApp>? = null,
    val timeStamp: Long
)
