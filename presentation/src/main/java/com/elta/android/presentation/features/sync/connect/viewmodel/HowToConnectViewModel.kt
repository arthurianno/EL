package com.elta.android.presentation.features.sync.connect.viewmodel

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.camera.lifecycle.ExperimentalCameraProviderConfiguration
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.elta.android.domain.features.diary.home.interactor.GetLocationNeededUseCase
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.Resource
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.params.TurningResultParam
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.viewmodel.ComposeScreenConfigurable
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.features.sync.connect.IS_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.model.howtoconnect.HowToConnectAction
import com.elta.android.presentation.features.sync.connect.model.howtoconnect.HowToConnectEvent
import com.elta.android.presentation.features.sync.connect.model.howtoconnect.HowToConnectViewState
import com.elta.android.presentation.utils.cacheHelper.ImageCacheHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

@ExperimentalCameraProviderConfiguration
class HowToConnectViewModel @Inject constructor(
    private val getLocationNeededUseCase: GetLocationNeededUseCase,
    private val appMetric: AppMetricTracker,
    private val getScreenFromCacheUseCase: GetScreenConfigFromCache,
    private val context: Context,
) : BaseViewModel<HowToConnectViewState>(), ComposeScreenConfigurable {

    override val screenConfigKey = "connect-start-onboarding"
    override val getScreenConfigUseCase = getScreenFromCacheUseCase


    override fun createInitState(): HowToConnectViewState =
        HowToConnectViewState(
            isOnBoarding = false
        )

    val appTopBar = BaseAppTopBarWidgetModel()
    val downButton = DownButtonWidgetModel()

    val cameraPermissionDialog = BaseDialogWidgetModel<Nothing>(
        positiveOnCLick = { sendEvent(HowToConnectEvent.OpenSettings) }
    )

    val locationPermissionDialog = BaseDialogWidgetModel<Nothing>(
        positiveOnCLick = { sendEvent(HowToConnectEvent.OpenSettings) }
    )

    val bluetoothPermissionDialog = BaseDialogWidgetModel<Nothing>(
        positiveOnCLick = { sendEvent(HowToConnectEvent.OpenSettings) }
    )

    override val widgets = listOf(
        appTopBar,
        downButton
    ).actionObserve()


    init {
        loadScreenConfig(
            context = context,
            updateState = { screenEntity, isContentReady ->
                state.value.copy(
                    screenConfig = screenEntity,
                    isContentReady = isContentReady
                )
            }
        )
    }

    override fun handleFragmentArguments(arguments: Bundle) {
        reduceState {
            state.value.copy(
                isOnBoarding = arguments.getBoolean(
                    IS_ON_BOARDING_ARGUMENT_NAME
                )
            )
        }
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            is AppAction.BackPressure -> backClick()
            is HowToConnectAction.OnConnectButtonClick -> sendEvent(HowToConnectEvent.RequestCameraPermission)

            is HowToConnectAction.Camera.AppearPermission -> appMetric.trackEvent(AppMetricEvent.Permission.Alert.Camera)
            is HowToConnectAction.Bluetooth.AppearPermission -> appMetric.trackEvent(AppMetricEvent.Permission.Alert.Bluetooth)
            is HowToConnectAction.Location.AppearPermission -> appMetric.trackEvent(AppMetricEvent.Permission.Alert.Location)

            is HowToConnectAction.Camera.AllowPermission -> {
                if (!action.isAlreadyGranted) appMetric.trackEvent(AppMetricEvent.Permission.Alert.Camera)
                checkBluetoothPermission()
            }

            is HowToConnectAction.Bluetooth.AllowPermission -> {
                if (!action.isAlreadyGranted) appMetric.trackEvent(AppMetricEvent.Permission.Alert.Bluetooth)
                launch { checkLocationPermission() }
            }

            is HowToConnectAction.Location.AllowPermission -> {
                if (!action.isAlreadyGranted) appMetric.trackEvent(AppMetricEvent.Permission.Alert.Location)
                appMetric.trackEvent(AppMetricEvent.BluetoothTurningAlert)

                sendEvent(HowToConnectEvent.Bluetooth.Enable)
            }

            is HowToConnectAction.Camera.ShowPermissionRationale -> cameraPermissionDialog.dialogOpen()
            is HowToConnectAction.Bluetooth.ShowPermissionRationale -> bluetoothPermissionDialog.dialogOpen()
            is HowToConnectAction.Location.ShowPermissionRationale -> locationPermissionDialog.dialogOpen()

            is HowToConnectAction.Bluetooth.Enabled -> {
                appMetric.trackEvent(AppMetricEvent.BluetoothTurningAlertClick(TurningResultParam.ALLOW))
                launch { checkLocationEnabled() }
            }
            is HowToConnectAction.Bluetooth.Rejected -> {
                appMetric.trackEvent(AppMetricEvent.BluetoothTurningAlertClick(TurningResultParam.REJECT))
            }
            is HowToConnectAction.Location.Enabled -> navigateToCameraScreen()
        }
    }

    private suspend fun checkLocationEnabled() {
        val isLocationNeeded = getLocationNeededUseCase.execute().await()

        if (isLocationNeeded) sendEvent(HowToConnectEvent.Location.Enable)
        else navigateToCameraScreen()
    }

    private fun checkBluetoothPermission() {
        val event =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) HowToConnectEvent.Bluetooth.RequestPermission
            else HowToConnectEvent.Location.RequestPermission
        sendEvent(event)
    }

    private suspend fun checkLocationPermission() {
        val isLocationNeeded = getLocationNeededUseCase.execute().await()

        val event =
            if (isLocationNeeded) HowToConnectEvent.Location.RequestPermission
            else {
                appMetric.trackEvent(AppMetricEvent.BluetoothTurningAlert)
                HowToConnectEvent.Bluetooth.Enable
            }
        sendEvent(event)
    }

    private fun navigateToCameraScreen() {
        router.navigateTo(Screens.ScannerDmcScreen(state.value.isOnBoarding))
    }
}
