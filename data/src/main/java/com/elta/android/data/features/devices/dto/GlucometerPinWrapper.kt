package com.elta.android.data.features.devices.dto

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class GlucometerPinWrapper(
    @Id(assignable = true) var id: Long,
    val pin: String
)