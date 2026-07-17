package com.elta.android.domain.features.consultant.model

data class UserState(
    val currentScenarioId: String? = null,
    val currentNodeId: String? = null,
    val historyStack: List<String> = emptyList()
)
