package com.elta.android.presentation.features.devices.firmware.pm

import com.elta.android.presentation.R
import com.nullgr.core.resources.ResourceProvider

sealed class UpdateState {

    abstract val icon: Int
    abstract val title: String
    abstract val description: String?
    abstract val hint: String?
    abstract val button: String?

    data class Progress(
        val resources: ResourceProvider,
        val currentVersion: String? = null,
        override val icon: Int = R.drawable.ic_firmware_logo,
        override val title: String = resources.getString(R.string.firmware_title_checking_updates),
        override val description: String? = currentVersion?.let {
            resources.getString(R.string.firmware_version_current, it)
        },
        override val hint: String? = resources.getString(R.string.firmware_updates_hint),
        override val button: String? = null
    ) : UpdateState()

    data class NotFound(
        val resources: ResourceProvider,
        val currentVersion: String? = null,
        override val icon: Int = R.drawable.ic_firmware_logo,
        override val title: String = resources.getString(R.string.firmware_title_updates_not_found),
        override val description: String? = currentVersion?.let {
            resources.getString(R.string.firmware_version_current, it)
        },
        override val hint: String? = resources.getString(R.string.firmware_updates_hint),
        override val button: String? = null
    ) : UpdateState()

    data class Found(
        val resources: ResourceProvider,
        val newVersion: String,
        val currentVersion: String? = null,
        override val icon: Int = R.drawable.ic_firmware_logo,
        override val title: String = resources.getString(
            R.string.firmware_title_updates_found,
            newVersion
        ),
        override val description: String? = currentVersion?.let {
            resources.getString(R.string.firmware_version_current, it)
        },
        override val hint: String? = resources.getString(R.string.firmware_updates_hint),
        override val button: String? = resources.getString(R.string.firmware_button_update)
    ) : UpdateState()

    data class Downloading(
        val resources: ResourceProvider,
        val currentVersion: String? = null,
        override val icon: Int = R.drawable.ic_firmware_logo,
        override val title: String = resources.getString(R.string.firmware_title_downloading),
        override val description: String? = currentVersion?.let {
            resources.getString(R.string.firmware_version_current, it)
        },
        override val hint: String? = null,
        override val button: String? = null
    ) : UpdateState()

    data class Updating(
        val resources: ResourceProvider,
        val currentVersion: String? = null,
        override val icon: Int = R.drawable.ic_firmware_logo,
        override val title: String = resources.getString(R.string.firmware_title_updating),
        override val description: String? = currentVersion?.let {
            resources.getString(R.string.firmware_version_current, it)
        },
        override val hint: String? = null,
        override val button: String? = null
    ) : UpdateState()

    data class BatteryLowLevel(
        val resources: ResourceProvider,
        override val icon: Int = R.drawable.ic_firmware_error_low_battery,
        override val title: String = resources.getString(R.string.firmware_title_low_level),
        override val description: String? = resources.getString(R.string.firmware_description_low_level),
        override val hint: String? = null,
        override val button: String? = resources.getString(R.string.firmware_button_close)
    ) : UpdateState()

    data class FirmwareDownloadingError(
        val resources: ResourceProvider,
        override val icon: Int = R.drawable.ic_firmware_error_general,
        override val title: String = resources.getString(R.string.firmware_downloading_error_title),
        override val description: String? = resources.getString(R.string.firmware_downloading_error_description),
        override val hint: String? = null,
        override val button: String? = resources.getString(R.string.firmware_downloading_error_button)
    ) : UpdateState()

    data class FirmwareUpdateError(
        val resources: ResourceProvider,
        override val icon: Int = R.drawable.ic_firmware_error_general,
        override val title: String = resources.getString(R.string.firmware_update_error_title),
        override val description: String? = resources.getString(R.string.firmware_update_error_description),
        override val hint: String? = null,
        override val button: String? = resources.getString(R.string.firmware_update_error_button)
    ) : UpdateState()

    data class GlucometerOfflineError(
        val resources: ResourceProvider,
        override val icon: Int = R.drawable.ic_firmware_error_general,
        override val title: String = resources.getString(R.string.firmware_offline_error_title),
        override val description: String? = resources.getString(R.string.firmware_offline_error_description),
        override val hint: String? = null,
        override val button: String? = resources.getString(R.string.firmware_offline_error_button)
    ) : UpdateState()

    data class Updated(
        val resources: ResourceProvider,
        val newVersion: String? = null,
        override val icon: Int = R.drawable.ic_firmware_logo,
        override val title: String = resources.getString(R.string.firmware_title_updated),
        override val description: String? = newVersion?.let {
            resources.getString(R.string.firmware_version_new, it)
        },
        override val hint: String? = null,
        override val button: String? = null
    ) : UpdateState()
}
