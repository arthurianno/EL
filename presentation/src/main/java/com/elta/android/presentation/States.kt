package com.elta.android.presentation

import com.elta.android.presentation.core.ui.state_view.StateData

sealed class States : StateData {

    data class SimpleError(
        override val icon: Int?,
        override val title: String? = null,
        override val description: String?,
        override val button: String? = null
    ) : States()
}