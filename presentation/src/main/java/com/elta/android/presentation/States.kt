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

    data class MainRecordsScreenFirstLaunchState(
        val resources: ResourceProvider,
        override val icon: Int? = null,
        override val title: String = resources.getString(R.string.main_records_firs_launch_title),
        override val description: String = resources.getString(R.string.main_records_firs_launch_subtitle),
        override val button: String? = null
    ) : States()

    data class MainRecordsScreenNewDayState(
        val resources: ResourceProvider,
        val titleRes: Int,
        override val icon: Int? = null,
        override val title: String = resources.getString(titleRes),
        override val description: String = resources.getString(R.string.main_records_new_day_subtitle),
        override val button: String? = null
    ) : States()
}
