package com.elta.android.domain.features.observers.model

import com.elta.android.domain.features.user.model.State

data class Observer(
    val id: String,
    val email: String,
    val name: String?,
    val customName: String?,
    val status: ObserverStatus,
    val modificationTime: Long?,
    val state: State
)