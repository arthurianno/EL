package com.elta.android.presentation.features.sync.connect.pin

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.buttons.GradientActionButton
import com.elta.android.presentation.features.sync.connect.base.ui.adapter.items.DeviceItem
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalTypes

internal const val PIN_ANIMATION_TAG = "pin-animation"
internal const val PIN_DISPLAY_TAG = "pin-display-input"
internal const val PIN_ACTION_PANEL_TAG = "pin-action-panel"

internal data class PinConnectionUiState(
    val stage: PinConnectionStage = PinConnectionStage.HOW_TO_CONNECT,
    val devices: List<DeviceItem> = emptyList(),
    val canConnect: Boolean = false,
    val pin: String = "",
    val pinIncorrect: Boolean = false,
    val syncConfig: ScreenEntity? = null,
    val completeConfig: ScreenEntity? = null,
    val errorConfig: ScreenEntity? = null
) {
    val actionEnabled: Boolean get() = when (stage) {
        PinConnectionStage.SEARCH, PinConnectionStage.CONNECTING, PinConnectionStage.CONNECTED -> false
        PinConnectionStage.FOUND -> canConnect
        PinConnectionStage.ENTER_PIN -> pin.isGlucometerPinValid()
        else -> true
    }
}

private data class PinStageText(
    @field:StringRes val title: Int,
    @field:StringRes val description: Int?,
    @field:StringRes val button: Int
)

private fun PinConnectionStage.text() = when (this) {
    PinConnectionStage.HOW_TO_CONNECT -> PinStageText(R.string.sync_pin_intro_title, null, R.string.sync_pin_start)
    PinConnectionStage.SEARCH -> PinStageText(R.string.sync_pin_search_title, R.string.sync_pin_search_description, R.string.sync_pin_connect)
    PinConnectionStage.FOUND -> PinStageText(R.string.sync_pin_choose_title, R.string.sync_pin_choose_description, R.string.sync_pin_connect)
    PinConnectionStage.ENTER_PIN -> PinStageText(R.string.sync_pin_input_title, R.string.sync_pin_input_description, R.string.sync_pin_confirm)
    PinConnectionStage.CONNECTING -> PinStageText(R.string.sync_dmc_connecting_title, R.string.sync_dmc_connecting_description, R.string.sync_pin_start)
    PinConnectionStage.CONNECTED -> PinStageText(R.string.sync_dmc_sync_title, R.string.sync_dmc_sync_description, R.string.sync_pin_start)
    PinConnectionStage.SYNC_COMPLETED -> PinStageText(R.string.sync_dmc_complete_title, R.string.sync_dmc_complete_description, R.string.sync_pin_start)
    PinConnectionStage.CONNECT_ERROR -> PinStageText(R.string.sync_pin_connect_error_title, R.string.sync_dmc_connect_error_description, R.string.sync_pin_retry_connect)
    PinConnectionStage.SYNC_ERROR -> PinStageText(R.string.sync_pin_sync_error_title, R.string.sync_dmc_sync_error_description, R.string.sync_pin_retry_sync)
    PinConnectionStage.NOT_FOUND -> PinStageText(R.string.sync_pin_not_found_title, R.string.sync_pin_not_found_description, R.string.sync_pin_retry_search)
}

@Composable
internal fun PinConnectionScreen(
    state: PinConnectionUiState,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onSelectDevice: (DeviceItem) -> Unit,
    onPinChanged: (String) -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    val types = LocalTypes.current
    val stageText = state.stage.text()
    val enteringPin = state.stage == PinConnectionStage.ENTER_PIN
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    LaunchedEffect(state.stage) {
        if (!enteringPin) {
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }
    val config = when (state.stage) {
        PinConnectionStage.CONNECTED -> state.syncConfig
        PinConnectionStage.SYNC_COMPLETED -> state.completeConfig
        PinConnectionStage.SYNC_ERROR -> state.errorConfig
        else -> null
    }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.white, Color(0xFFF9FCFA), colors.white)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .clipToBounds()
    ) {
        val density = LocalDensity.current
        val imeVisible = WindowInsets.ime.getBottom(density) > 0
        val fontScale = density.fontScale
        var headerBottom by remember { mutableStateOf(48.dp + 112.dp * fontScale) }
        var footerHeight by remember { mutableStateOf(80.dp) }
        var unobscuredHeight by remember(maxWidth) { mutableStateOf(maxHeight) }
        SideEffect {
            if (!imeVisible && !enteringPin) unobscuredHeight = maxHeight
        }
        val normal = normalPinIllustrationLayout(
            maxWidth,
            if (imeVisible || enteringPin) unobscuredHeight else maxHeight
        )
        val target = if (enteringPin) enlargedPinIllustrationLayout(normal, maxHeight, headerBottom, footerHeight) else normal
        // IME insets already animate the viewport. A second animation trails the
        // keyboard and temporarily lets the LCD slide underneath the action panel.
        val artWidth by animateDpAsState(target.width, if (enteringPin && imeVisible) snap() else tween(250), label = "pin-canvas-width")
        val artTop by animateDpAsState(target.top, if (enteringPin && imeVisible) snap() else tween(if (enteringPin) 1000 else 250), label = "pin-canvas-top")
        var pinReady by remember { mutableStateOf(false) }
        val headerMaxHeight = if (enteringPin) minOf(112.dp * fontScale, maxHeight * 0.4f)
        else (normal.meterTop - 60.dp).coerceAtLeast(0.dp)

        PinConnectionAnimation(
            stage = state.stage,
            onPinReady = { pinReady = it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = artTop)
                .wrapContentSize(Alignment.TopCenter, unbounded = true)
                .requiredSize(artWidth, artWidth * 1.5f)
                .testTag(PIN_ANIMATION_TAG)
        )
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().background(colors.white).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painterResource(R.drawable.ic_back),
                        contentDescription = stringResource(R.string.content_description_back_button),
                        tint = colors.blackBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onHelp) {
                    Text(
                        stringResource(R.string.sync_connect_type_button_need_help),
                        style = types.caption1.copy(fontSize = 14.sp),
                        color = colors.shadeBlack1
                    )
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned {
                        headerBottom = with(density) { (it.positionInParent().y + it.size.height).toDp() }
                    }
                    .heightIn(max = headerMaxHeight)
                    .verticalScroll(rememberScrollState())
                    .background(colors.white)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            ) {
                Text(
                    config?.title?.takeIf(String::isNotBlank) ?: stringResource(stageText.title),
                    style = types.h0.copy(lineHeight = 28.sp),
                    color = colors.black
                )
                Spacer(Modifier.height(9.dp))
                if (state.stage == PinConnectionStage.HOW_TO_CONNECT) {
                    PinInstruction("1.", stringResource(R.string.sync_pin_intro_step_bluetooth))
                    Spacer(Modifier.height(16.dp))
                    PinInstruction("2.", stringResource(R.string.sync_pin_intro_step_battery))
                } else {
                    stageText.description?.let {
                        Text(
                            config?.description?.takeIf(String::isNotBlank) ?: stringResource(it),
                            style = types.subtitle2.copy(lineHeight = if (state.stage == PinConnectionStage.NOT_FOUND) 20.sp else 19.sp),
                            color = colors.black.copy(alpha = 0.65f)
                        )
                    }
                }
                if (state.stage == PinConnectionStage.FOUND) {
                    Spacer(Modifier.height(20.dp))
                    PinDeviceList(state.devices, onSelectDevice)
                } else if (state.stage in setOf(PinConnectionStage.CONNECTING, PinConnectionStage.CONNECTED, PinConnectionStage.SYNC_COMPLETED)) {
                    Spacer(Modifier.height(32.dp))
                    Divider(color = colors.shadeBlack3, modifier = Modifier.padding(horizontal = 8.dp))
                }
                if (enteringPin && state.pinIncorrect) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.sync_connect_incorrect_pin_code),
                        color = colors.red,
                        style = types.descriptionError,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Column(
                Modifier.fillMaxWidth()
                    .testTag(PIN_ACTION_PANEL_TAG)
                    .onSizeChanged { footerHeight = with(density) { it.height.toDp() } }
                    .background(colors.white)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = if (enteringPin && imeVisible) 8.dp else 20.dp)
            ) {
                if (state.stage == PinConnectionStage.HOW_TO_CONNECT) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.ic_bluetooth),
                            contentDescription = null,
                            tint = colors.gGreenB,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.sync_pin_bluetooth_indicator),
                            style = types.subtitle2,
                            color = colors.gGreenB
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
                GradientActionButton(
                    text = stringResource(stageText.button),
                    enabled = state.actionEnabled,
                    isLoading = false,
                    shape = 10,
                    onClick = onAction
                )
            }
        }
        if (enteringPin && pinReady) {
            PinDisplayInput(
                pin = state.pin,
                incorrect = state.pinIncorrect,
                onPinChanged = onPinChanged,
                onConfirm = { if (state.actionEnabled) onAction() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = artWidth * 0.007f, y = artTop + artWidth * 0.373f)
                    .size(artWidth * 0.524f, artWidth * 0.352f)
                    .testTag(PIN_DISPLAY_TAG)
            )
        }
    }
}

@Composable
private fun PinInstruction(number: String, instruction: String) {
    val types = LocalTypes.current
    val color = LocalColors.current.black.copy(alpha = 0.65f)
    Row {
        Text(number, style = types.subtitle2.copy(lineHeight = 19.sp), color = color, modifier = Modifier.width(24.dp))
        Text(instruction, style = types.subtitle2.copy(lineHeight = 19.sp), color = color, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PinDeviceList(devices: List<DeviceItem>, onSelect: (DeviceItem) -> Unit) {
    val colors = LocalColors.current
    Column(Modifier.fillMaxWidth().heightIn(max = 160.dp).verticalScroll(rememberScrollState())) {
        Divider(color = colors.shadeBlack3, modifier = Modifier.padding(start = 20.dp))
        devices.forEach { device ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(selected = device.isSelected, role = Role.RadioButton, onClick = { onSelect(device) })
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.ic_pin_device),
                    contentDescription = null,
                    tint = if (device.isSelected) colors.gGreenB else colors.blackBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(11.dp))
                Text(
                    device.name,
                    style = LocalTypes.current.h3.copy(fontSize = 17.sp),
                    color = if (device.isSelected) colors.gGreenB else colors.black.copy(alpha = 0.45f),
                    modifier = Modifier.weight(1f).padding(vertical = 8.dp)
                )
            }
            Divider(color = colors.shadeBlack3, modifier = Modifier.padding(start = 20.dp))
        }
    }
}

@Composable
private fun PinDisplayInput(
    pin: String,
    incorrect: Boolean,
    onPinChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val requester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val preview = LocalInspectionMode.current
    val label = stringResource(R.string.sync_pin_input_label)
    val errorMessage = stringResource(R.string.sync_connect_incorrect_pin_code)
    LaunchedEffect(Unit) {
        if (!preview) {
            requester.requestFocus()
            withFrameNanos { }
            keyboard?.show()
        }
    }
    BasicTextField(
        value = pin,
        onValueChange = { onPinChanged(it.asGlucometerPin()) },
        singleLine = true,
        textStyle = LocalTypes.current.h0.copy(color = Color.Transparent, textAlign = TextAlign.Center),
        cursorBrush = SolidColor(Color.Transparent),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onConfirm() }),
        modifier = modifier.focusRequester(requester).semantics {
            contentDescription = label
            if (incorrect) error(errorMessage)
        },
        decorationBox = { input ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                input()
                Canvas(Modifier.fillMaxSize()) { drawPinCode(pin) }
            }
        }
    )
}

@Preview(name = "Подключение по ПИН", widthDp = 375, heightDp = 812, showBackground = true, locale = "ru")
@Composable
private fun PinConnectionIntroPreview() = EltaTheme {
    PinConnectionScreen(PinConnectionUiState(), {}, {}, {}, {}, {})
}

@Preview(name = "ПИН-код", widthDp = 375, heightDp = 812, showBackground = true, locale = "ru")
@Composable
private fun PinConnectionInputPreview() = EltaTheme {
    PinConnectionScreen(PinConnectionUiState(stage = PinConnectionStage.ENTER_PIN, pin = "376"), {}, {}, {}, {}, {})
}
