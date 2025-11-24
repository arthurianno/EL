package com.elta.android.presentation.features.consultant.model

import androidx.compose.runtime.Immutable
import org.threeten.bp.LocalTime

@Immutable
data class RecordGraphState(
    val recordState: RecordState,
    val recordGraph: List<Float>,
    val duration: LocalTime
)
