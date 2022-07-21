package com.elta.android.data.features.user.cache.dto

import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany

@Entity
data class ProfileCacheDto(
    @Id(assignable = true) var id: Long,
    val diabetes: String?,
    val weight: Double?,
    val gender: String?,
    val email: String?,
    val timeStamp: Long,

    // represents PersonDto
    val firstName: String?,
    val lastName: String?,

    // represents GlucoseLevelDto
    val minBeforeEatingValue: Double?,
    val maxBeforeEatingValue: Double?,
    val minAfterEatingValue: Double?,
    val maxAfterEatingValue: Double?,
    val minAverageValue: Double?,
    val maxAverageValue: Double?
) {
    @Backlink(to = "profile")
    lateinit var socialNetworks: ToMany<NetworkCacheDto>

    @Backlink(to = "profile")
    lateinit var healthApps: ToMany<HealthAppCacheDto>

    @Transient
    var tempSocialNetworks: List<NetworkCacheDto> = emptyList()

    @Transient
    var tempHealthApps: List<HealthAppCacheDto> = emptyList()
}
