package com.elta.android.presentation.features.app.model

import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.status.Status
import com.nullgr.core.resources.ResourceProvider

sealed class SyncStatus : Status {
    sealed class Glucometer : SyncStatus() {
        data class Started(
            val resources: ResourceProvider,
            override val text: String = resources.getString(R.string.sync_with_glucometer_in_progress),
            override val color: Int = resources.getColor(R.color.color_background_sync_started)
        ) : SyncStatus()

        data class Success(
            val resources: ResourceProvider,
            override val text: String = resources.getString(R.string.sync_with_glucometer_completed),
            override val color: Int = resources.getColor(R.color.color_background_sync_finished)
        ) : SyncStatus()

        data class Error(
            val resources: ResourceProvider,
            override val text: String = resources.getString(R.string.sync_with_glucometer_error),
            override val color: Int = resources.getColor(R.color.color_background_sync_error)
        ) : SyncStatus()

        data class NoNewEvents(
            val resources: ResourceProvider,
            override val text: String = resources.getString(R.string.sync_with_glucometer_no_new_events),
            override val color: Int = resources.getColor(R.color.black)
        ) : SyncStatus()
    }

    sealed class Server : SyncStatus() {
        data class Started(
            val resources: ResourceProvider,
            override val text: String = resources.getString(R.string.sync_with_backend_in_progress),
            override val color: Int = resources.getColor(R.color.color_background_backend_sync_started)
        ) : SyncStatus()

        data class Success(
            val resources: ResourceProvider,
            override val text: String = resources.getString(R.string.sync_with_backend_complete),
            override val color: Int = resources.getColor(R.color.color_background_backend_sync_finished)
        ) : SyncStatus()

        data class Error(
            val resources: ResourceProvider,
            override val text: String = resources.getString(R.string.sync_with_backend_error),
            override val color: Int = resources.getColor(R.color.color_background_sync_error)
        ) : SyncStatus()

        data class ErrorWithMessage(
            val resources: ResourceProvider,
            override val text: String = resources.getString(R.string.sync_with_backend_error_try_later),
            override val color: Int = resources.getColor(R.color.black)
        ) : SyncStatus()
    }

    data class Email(
        val resources: ResourceProvider,
        override val text: String = resources.getString(R.string.error_verify_your_email),
        override val color: Int = resources.getColor(R.color.black)
    ) : SyncStatus()

    data class NetworkProblemTryLater(
        val resources: ResourceProvider,
        override val text: String = resources.getString(R.string.error_network_try_later),
        override val color: Int = resources.getColor(R.color.black)
    ) : SyncStatus()

    data class SomethingWentWrong(
        val resources: ResourceProvider,
        override val text: String = resources.getString(R.string.error_something_went_wrong),
        override val color: Int = resources.getColor(R.color.black)
    ) : SyncStatus()
}
