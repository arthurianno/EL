package com.elta.android.presentation

import com.elta.android.presentation.core.bus.Event

sealed class Events : Event {

    data class WeightSelected(val newWeight: Double?) : Events()
}