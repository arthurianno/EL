package com.elta.android.presentation

import com.elta.android.presentation.core.ui.state_view.StateData
import com.nullgr.core.resources.ResourceProvider

sealed class States : StateData {

    data class SimpleError(
        override val icon: Int?,
        override val title: String? = null,
        override val description: String?,
        override val button: String? = null
    ) : States()

    data class ServerError(
        val resources: ResourceProvider,
        override val icon: Int? = R.drawable.ic_server_error,
        override val title: String? = resources.getString(R.string.error_server_not_responding_title),
        override val description: String? = resources.getString(R.string.error_server_not_responding_subtitle),
        override val button: String? = resources.getString(R.string.error_server_not_responding_retry_button)
    ) : States()
}