package com.elta.android.domain.features.observers.model

import com.elta.android.domain.features.user.model.State
import java.util.Date

data class Observer(
    val id: String,
    val email: String,
    val name: String?,
    val status: ObserverStatus,
    val modificationTime: Date?,
    val state: State
)