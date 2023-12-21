package com.elta.android.data.features.common.storage

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class SyncInfoDto(
    @Id(assignable = true) var id: Long = SyncInfoDto::class.java.simpleName.hashCode().toLong(),
    val lastSalePointsSync: Long? = null,
    val lastEventsSync: Long? = null,
    val lastTagsSync: Long? = null,
    val lastGoogleFitSync: Long? = null,
    val lastMedicamentSync: Long? = null,
)
