package com.elta.android.domain.features.devices.model

enum class BootModeStatus {
    Progress, Completed, UpdateFailed, SyncFailed;

    companion object {
        const val ACTION_STATUS_NAME = "action_boot_mode_status"
        const val STATUS_NAME_KEY = "status_name"
    }
}
