package com.elta.android.presentation.features.sync.connect

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.buttons.GradientActionButton
import com.elta.android.presentation.features.sync.connect.model.connecting.ConnectingStageType
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalTypes
import kotlin.math.roundToInt

private val progressColor = Color(0xFF3EC1C5)
private val errorColor = Color(0xFFCA2F2F)

private data class StagePresentation(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @RawRes val animation: Int,
    @StringRes val phoneTitle: Int,
    @StringRes val phoneSubtitle: Int? = null,
    @StringRes val button: Int? = null,
    val isError: Boolean = false,
    val isLoading: Boolean = false
)

private fun ConnectingStageType.presentation() = when (this) {
    ConnectingStageType.Connecting -> StagePresentation(
        title = R.string.sync_dmc_connecting_title,
        description = R.string.sync_dmc_connecting_description,
        animation = R.raw.dmc_bluetooth,
        phoneTitle = R.string.sync_dmc_wait,
        phoneSubtitle = R.string.sync_dmc_connecting_status,
        isLoading = true
    )

    ConnectingStageType.Sync -> StagePresentation(
        title = R.string.sync_dmc_sync_title,
        description = R.string.sync_dmc_sync_description,
        animation = R.raw.dmc_synchronization,
        phoneTitle = R.string.sync_dmc_wait,
        phoneSubtitle = R.string.sync_dmc_sync_status,
        isLoading = true
    )

    ConnectingStageType.Complete -> StagePresentation(
        title = R.string.sync_dmc_complete_title,
        description = R.string.sync_dmc_complete_description,
        animation = R.raw.dmc_synchronization_done,
        phoneTitle = R.string.sync_dmc_success_status,
        button = R.string.sync_dmc_next
    )

    ConnectingStageType.DeviceNotFound -> StagePresentation(
        title = R.string.profile_device_search_not_found_title,
        description = R.string.profile_device_search_not_found_connect_title,
        animation = R.raw.dmc_bluetooth_error,
        phoneTitle = R.string.sync_dmc_not_found_status,
        phoneSubtitle = R.string.sync_dmc_fix_reasons,
        button = R.string.repeat_search_button_text,
        isError = true
    )

    ConnectingStageType.ErrorConnect -> StagePresentation(
        title = R.string.sync_connection_connect_error_title,
        description = R.string.sync_dmc_connect_error_description,
        animation = R.raw.dmc_bluetooth_error,
        phoneTitle = R.string.sync_dmc_error_status,
        phoneSubtitle = R.string.sync_dmc_retry_later,
        button = R.string.repeat_connect_button_text,
        isError = true
    )

    ConnectingStageType.ErrorSync -> StagePresentation(
        title = R.string.sync_connection_sync_error_title,
        description = R.string.sync_dmc_sync_error_description,
        animation = R.raw.dmc_synchronization_error,
        phoneTitle = R.string.sync_dmc_error_status,
        phoneSubtitle = R.string.sync_dmc_retry_later,
        button = R.string.repeat_sync_button_text,
        isError = true
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun DmcConnectionProgressScreen(
    stage: ConnectingStageType,
    onBack: () -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    onConnectByPin: (() -> Unit)? = null
) {
    val colors = LocalColors.current
    val types = LocalTypes.current
    val presentation = stage.presentation()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val scope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetBackgroundColor = colors.white,
        sheetContent = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.sync_connection_help_sheet_title),
                        style = types.h2.copy(fontSize = 18.sp, lineHeight = 22.sp),
                        color = colors.black,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { scope.launch { sheetState.hide() } },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_dialog_close_profile),
                            contentDescription = stringResource(R.string.content_description_close_button),
                            tint = Color(0xFF878B93)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.sync_connection_help_sheet_text),
                    style = types.body1.copy(fontSize = 15.sp, lineHeight = 20.sp),
                    color = colors.shadeBlack0
                )
                Spacer(Modifier.height(24.dp))
                if (onConnectByPin != null) {
                    GradientActionButton(
                        text = stringResource(R.string.sync_connect_by_pin_boton_text),
                        enabled = true,
                        isLoading = false,
                        shape = 10,
                        onClick = {
                            scope.launch { sheetState.hide() }
                            onConnectByPin()
                        }
                    )
                }
            }
        },
        modifier = modifier
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(colors.white)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 8.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (stage != ConnectingStageType.Complete) {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(R.drawable.ic_back),
                                contentDescription = stringResource(R.string.content_description_back_button),
                                tint = colors.blackBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Spacer(Modifier.size(48.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    if (stage != ConnectingStageType.Complete) {
                        TextButton(onClick = { scope.launch { sheetState.show() } }) {
                            Text(
                                text = stringResource(
                                    if (presentation.isError) R.string.sync_connect_type_button_any_difficulties
                                    else R.string.sync_connect_type_button_need_help
                                ),
                                style = types.caption1,
                                color = colors.shadeBlack1
                            )
                        }
                    }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                ) {
                    Text(
                        text = stringResource(presentation.title),
                        style = types.h0.copy(lineHeight = 28.sp),
                        color = colors.black
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(presentation.description),
                        style = types.body1.copy(lineHeight = 20.sp),
                        color = colors.shadeBlack0
                    )
                    if (stage == ConnectingStageType.DeviceNotFound) {
                        Spacer(Modifier.height(8.dp))
                        DmcReason("1.", R.string.profile_device_search_not_found_disable_ble)
                        DmcReason("2.", R.string.profile_device_search_not_found_low_energy)
                        DmcReason("3.", R.string.profile_device_search_not_found_out_of_range)
                    }
                }

                BoxWithConstraints(
                    Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val availableHeight = maxHeight
                    if (availableHeight > 100.dp) {
                        Box(Modifier.padding(bottom = 32.dp)) {
                            DmcPhoneStage(
                                stage = stage,
                                presentation = presentation,
                                height = minOf(376.dp, availableHeight - 32.dp)
                            )
                        }
                    }
                }

                if (presentation.button != null) {
                    GradientActionButton(
                        text = stringResource(presentation.button),
                        enabled = true,
                        isLoading = false,
                        shape = 10,
                        onClick = onAction,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    )
                } else {
                    Spacer(Modifier.height(68.dp))
                }
            }
        }
    }
}

@Composable
private fun DmcReason(number: String, @StringRes text: Int) {
    val colors = LocalColors.current
    Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Text(number, style = LocalTypes.current.caption1, color = colors.shadeBlack0, modifier = Modifier.width(18.dp))
        Text(stringResource(text), style = LocalTypes.current.caption1, color = colors.shadeBlack0)
    }
}

@Composable
private fun DmcPhoneStage(stage: ConnectingStageType, presentation: StagePresentation, height: Dp) {
    val phoneWidth = height * (198f / 376f)
    val phoneImage = ImageBitmap.imageResource(R.drawable.img_dmc_phone_mockup)
    val cropOffset = IntOffset(
        (phoneImage.width * 205f / 941f).roundToInt(),
        (phoneImage.height * 250f / 1672f).roundToInt()
    )
    val cropSize = IntSize(
        (phoneImage.width * 530f / 941f).roundToInt(),
        (phoneImage.height * 1090f / 1672f).roundToInt()
    )
    Box(
        Modifier
            .width(phoneWidth)
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // The source PNG has wide blank margins around the phone.
            drawImage(
                image = phoneImage,
                srcOffset = cropOffset,
                srcSize = cropSize,
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
            )
        }
        DmcLottie(
            stage = stage,
            animation = presentation.animation,
            repeat = presentation.isLoading,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = height * 0.21f)
                .size(minOf(80.dp, height * 0.22f))
        )
        Column(
            Modifier.align(Alignment.BottomCenter).padding(start = 12.dp, end = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val textColor = if (presentation.isError) errorColor else progressColor
            Text(
                text = stringResource(presentation.phoneTitle),
                style = LocalTypes.current.h2.copy(fontSize = 16.sp, lineHeight = 20.sp),
                color = textColor,
                textAlign = TextAlign.Center
            )
            presentation.phoneSubtitle?.let {
                Text(
                    text = stringResource(it),
                    style = LocalTypes.current.caption1.copy(fontSize = 13.sp, lineHeight = 18.sp),
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private data class LottiePlayback(
    val stage: ConnectingStageType,
    val listener: AnimatorListenerAdapter? = null
)

@RawRes
private fun transitionAnimation(from: ConnectingStageType?, to: ConnectingStageType): Int? = when {
    from == ConnectingStageType.Connecting && to == ConnectingStageType.Sync -> R.raw.dmc_bluetooth_synchronization
    from == ConnectingStageType.Connecting && to in setOf(ConnectingStageType.DeviceNotFound, ConnectingStageType.ErrorConnect) -> R.raw.dmc_bluetooth_error
    from in setOf(ConnectingStageType.DeviceNotFound, ConnectingStageType.ErrorConnect) && to == ConnectingStageType.Connecting -> R.raw.dmc_error_bluetooth
    from == ConnectingStageType.Sync && to == ConnectingStageType.Complete -> R.raw.dmc_synchronization_done
    from == ConnectingStageType.Sync && to == ConnectingStageType.ErrorSync -> R.raw.dmc_synchronization_error
    from == ConnectingStageType.ErrorSync && to == ConnectingStageType.Sync -> R.raw.dmc_error_synchronization
    else -> null
}

private fun LottieAnimationView.showSteadyAnimation(@RawRes resource: Int, repeat: Boolean, preview: Boolean) {
    setAnimation(resource)
    repeatCount = if (repeat) LottieDrawable.INFINITE else 0
    if (preview) progress = if (repeat) 0.85f else 1f
    else if (repeat) playAnimation()
    else progress = 1f
}

@Composable
private fun DmcLottie(
    stage: ConnectingStageType,
    @RawRes animation: Int,
    repeat: Boolean,
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current
    AndroidView(
        factory = { context -> LottieAnimationView(context) },
        update = { view ->
            val previous = view.tag as? LottiePlayback
            if (previous?.stage != stage) {
                previous?.listener?.let(view::removeAnimatorListener)
                view.cancelAnimation()
                val transition = if (isPreview) null else transitionAnimation(previous?.stage, stage)
                if (transition == null) {
                    view.tag = LottiePlayback(stage)
                    view.showSteadyAnimation(animation, repeat, isPreview)
                } else {
                    val listener = object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animationValue: Animator) {
                            view.removeAnimatorListener(this)
                            if ((view.tag as? LottiePlayback)?.stage == stage) {
                                view.tag = LottiePlayback(stage)
                                view.showSteadyAnimation(animation, repeat, preview = false)
                            }
                        }
                    }
                    view.tag = LottiePlayback(stage, listener)
                    view.setAnimation(transition)
                    view.repeatCount = 0
                    view.addAnimatorListener(listener)
                    view.playAnimation()
                }
            }
        },
        modifier = modifier
    )
}

@Preview(name = "Подключение", showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun DmcConnectingPreview() = DmcProgressPreview(ConnectingStageType.Connecting)

@Preview(name = "Синхронизация", showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun DmcSyncPreview() = DmcProgressPreview(ConnectingStageType.Sync)

@Preview(name = "Готово", showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun DmcCompletePreview() = DmcProgressPreview(ConnectingStageType.Complete)

@Preview(name = "Устройство не найдено", showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun DmcNotFoundPreview() = DmcProgressPreview(ConnectingStageType.DeviceNotFound)

@Preview(name = "Ошибка подключения", showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun DmcConnectErrorPreview() = DmcProgressPreview(ConnectingStageType.ErrorConnect)

@Preview(name = "Ошибка синхронизации", showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun DmcSyncErrorPreview() = DmcProgressPreview(ConnectingStageType.ErrorSync)

@Composable
private fun DmcProgressPreview(stage: ConnectingStageType) {
    EltaTheme { DmcConnectionProgressScreen(stage, onBack = {}, onAction = {}) }
}
