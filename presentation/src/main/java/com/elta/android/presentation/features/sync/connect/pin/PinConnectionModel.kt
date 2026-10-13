package com.elta.android.presentation.features.sync.connect.pin

import com.elta.android.presentation.features.sync.connect.base.ui.adapter.items.DeviceItem
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import me.dmdev.rxpm.Action
import me.dmdev.rxpm.State

enum class PinConnectionStage {
    HOW_TO_CONNECT,
    SEARCH,
    FOUND,
    ENTER_PIN,
    CONNECTING,
    CONNECTED,
    SYNC_COMPLETED,
    CONNECT_ERROR,
    SYNC_ERROR,
    NOT_FOUND;

    val isSearching: Boolean get() = this == SEARCH || this == FOUND

    fun operationError(): PinConnectionStage = when (this) {
        SEARCH, FOUND -> NOT_FOUND
        CONNECTED -> SYNC_ERROR
        else -> CONNECT_ERROR
    }
}

internal const val GLUCOMETER_PIN_LENGTH = 3

internal fun String.isGlucometerPinValid(): Boolean =
    length == GLUCOMETER_PIN_LENGTH && all { it in '0'..'9' }

internal fun String.asGlucometerPin(): String =
    filter { it in '0'..'9' }.take(GLUCOMETER_PIN_LENGTH)

interface PinConnectionModel {
    val connectedScreenConfig: State<ScreenEntity?>
    val syncCompletedScreenConfig: State<ScreenEntity?>
    val syncErrorScreenConfig: State<ScreenEntity?>
    val connectState: State<PinConnectionStage>
    val connectDeviceEnabledState: State<Boolean>
    val pinInputState: State<String>
    val pinIncorrectState: State<Boolean>
    val selectDeviceAction: Action<DeviceItem>
    val pinChangedAction: Action<String>
    val submitPinAction: Action<Unit>
    val retryConnectAction: Action<Unit>
    val backHandleAction: Action<Unit>
    val startScanAction: Action<Unit>
    val connectDeviceAction: Action<Unit>
    val startSyncAction: Action<Unit>
    val toAppAction: Action<Unit>
}
