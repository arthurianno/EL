package com.elta.android.presentation.features.sync.connect.pin

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentPinConnectionBinding
import com.elta.android.presentation.features.sync.connect.base.ui.adapter.items.DeviceItem
import com.elta.android.presentation.theme.EltaTheme
import com.nullgr.core.ui.extensions.hide
import me.dmdev.rxpm.bindTo

abstract class PinConnectionFragment<T> :
    BaseFragment<T, FragmentPinConnectionBinding>(FragmentPinConnectionBinding::inflate)
    where T : BaseListPm, T : PinConnectionModel {

    override val screenLayout: Int = R.layout.fragment_pin_connection
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider
    override val applyBottomSystemInsets = false
    override val applyPlatformSystemWindowFitting = false

    private var uiState by mutableStateOf(PinConnectionUiState())
    private var connectionStatusVisibility: Int? = null
    private var syncStatusVisibility: Int? = null
    private var previousSoftInputMode: Int? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback { presentationModel.backHandleAction.consumer.accept(Unit) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        binding.root.setContent {
            EltaTheme {
                PinConnectionScreen(
                    state = uiState,
                    onBack = { presentationModel.backHandleAction.consumer.accept(Unit) },
                    onHelp = { router.navigateTo(Screens.ConnectHelpScreen) },
                    onSelectDevice = { presentationModel.selectDeviceAction.consumer.accept(it) },
                    onPinChanged = { presentationModel.pinChangedAction.consumer.accept(it) },
                    onAction = ::performAction
                )
            }
        }
        hideActivityStatusViews()
    }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        pm.connectState.bindTo { uiState = uiState.copy(stage = it) }
        pm.items.bindTo { uiState = uiState.copy(devices = it.filterIsInstance<DeviceItem>()) }
        pm.connectDeviceEnabledState.bindTo { uiState = uiState.copy(canConnect = it) }
        pm.pinInputState.bindTo { uiState = uiState.copy(pin = it) }
        pm.pinIncorrectState.bindTo { uiState = uiState.copy(pinIncorrect = it) }
        pm.connectedScreenConfig.bindTo { uiState = uiState.copy(syncConfig = it) }
        pm.syncCompletedScreenConfig.bindTo { uiState = uiState.copy(completeConfig = it) }
        pm.syncErrorScreenConfig.bindTo { uiState = uiState.copy(errorConfig = it) }
    }

    private fun performAction() {
        val pm = presentationModel
        when (uiState.stage) {
            PinConnectionStage.HOW_TO_CONNECT, PinConnectionStage.NOT_FOUND -> pm.startScanAction
            PinConnectionStage.FOUND -> pm.connectDeviceAction
            PinConnectionStage.ENTER_PIN -> pm.submitPinAction
            PinConnectionStage.CONNECT_ERROR -> pm.retryConnectAction
            PinConnectionStage.SYNC_ERROR -> pm.startSyncAction
            PinConnectionStage.SYNC_COMPLETED -> pm.toAppAction
            PinConnectionStage.SEARCH, PinConnectionStage.CONNECTING, PinConnectionStage.CONNECTED -> null
        }?.consumer?.accept(Unit)
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.let { window ->
            previousSoftInputMode = window.attributes.softInputMode
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        hideActivityStatusViews()
    }

    override fun onPause() {
        previousSoftInputMode?.let { activity?.window?.setSoftInputMode(it) }
        previousSoftInputMode = null
        restoreActivityStatusViews()
        super.onPause()
    }

    private fun hideActivityStatusViews() {
        val connectionView = activity?.findViewById<View>(R.id.connectionStatusView)
        val syncView = activity?.findViewById<View>(R.id.syncStatusView)
        if (connectionStatusVisibility == null) connectionStatusVisibility = connectionView?.visibility
        if (syncStatusVisibility == null) syncStatusVisibility = syncView?.visibility
        connectionView?.hide()
        syncView?.hide()
    }

    private fun restoreActivityStatusViews() {
        connectionStatusVisibility?.let { activity?.findViewById<View>(R.id.connectionStatusView)?.visibility = it }
        syncStatusVisibility?.let { activity?.findViewById<View>(R.id.syncStatusView)?.visibility = it }
        connectionStatusVisibility = null
        syncStatusVisibility = null
    }
}
