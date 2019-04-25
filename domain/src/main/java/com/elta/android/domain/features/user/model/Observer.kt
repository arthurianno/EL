package com.elta.android.domain.features.user.model

data class Observer(
    val id: String,
    val email: String,
    val name: String,
    val status: ObserverStatus
)