package com.elta.android.domain.features.observers.model


data class Observer(
    val id: String,
    val email: String,
    val name: String?,
    val customName: String?,
    val status: ObserverStatus
)
