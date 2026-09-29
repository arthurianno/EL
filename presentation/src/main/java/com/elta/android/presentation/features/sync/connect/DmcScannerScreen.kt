package com.elta.android.presentation.features.sync.connect

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalTypes

internal enum class DmcScannerUiState {
    Scanning,
    Error,
    AlreadyConnected,
    Help
}

@Composable
internal fun DmcScannerScreen(
    state: DmcScannerUiState,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onCloseHelp: () -> Unit,
    modifier: Modifier = Modifier,
    camera: @Composable () -> Unit
) {
    val mask = Color.Black.copy(alpha = 0.42f)

    BoxWithConstraints(modifier.fillMaxSize().background(Color.Black)) {
        camera()

        val frameSize = minOf(maxWidth - 80.dp, 280.dp, maxHeight * 0.45f)
        val frameTop = minOf(190.dp, maxHeight * 0.24f)
        val sideWidth = (maxWidth - frameSize) / 2

        Box(Modifier.fillMaxWidth().height(frameTop).background(mask))
        Box(
            Modifier
                .offset(y = frameTop + frameSize)
                .fillMaxWidth()
                .height(maxHeight - frameTop - frameSize)
                .background(mask)
        )
        Box(Modifier.offset(y = frameTop).width(sideWidth).height(frameSize).background(mask))
        Box(
            Modifier
                .offset(x = sideWidth + frameSize, y = frameTop)
                .width(sideWidth)
                .height(frameSize)
                .background(mask)
        )

        ScannerFrame(
            isError = state == DmcScannerUiState.Error,
            modifier = Modifier
                .offset(x = sideWidth, y = frameTop)
                .size(frameSize)
        )

        ScannerHeader(
            onBack = onBack,
            onHelp = onHelp,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        ScannerStatusPanel(
            state = state,
            onCloseHelp = onCloseHelp,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ScannerHeader(onBack: () -> Unit, onHelp: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalColors.current
    Row(
        modifier
            .fillMaxWidth()
            .background(Color(0xC9111216))
            .statusBarsPadding()
            .padding(start = 8.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = stringResource(R.string.content_description_back_button),
                tint = colors.white,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onHelp) {
            Text(
                text = stringResource(R.string.sync_connect_type_button_any_difficulties),
                style = LocalTypes.current.caption1,
                color = colors.white
            )
        }
    }
}

@Composable
internal fun ScannerFrame(isError: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalColors.current
    val tint = if (isError) colors.red else colors.shadeGGreenA
    Box(modifier) {
        ScannerCorner(R.drawable.img_border_corner, tint, 0f, Alignment.TopStart)
        ScannerCorner(R.drawable.img_border_corner, tint, 90f, Alignment.TopEnd)
        ScannerCorner(R.drawable.img_border_corner, tint, 180f, Alignment.BottomEnd)
        ScannerCorner(R.drawable.img_border_corner, tint, 270f, Alignment.BottomStart)
    }
}

@Composable
private fun BoxScope.ScannerCorner(
    @DrawableRes drawable: Int,
    tint: Color,
    rotation: Float,
    alignment: Alignment
) {
    Image(
        painter = painterResource(drawable),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tint),
        modifier = Modifier.align(alignment).rotate(rotation)
    )
}

@Composable
private fun ScannerStatusPanel(
    state: DmcScannerUiState,
    onCloseHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    val types = LocalTypes.current
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(colors.white)
    ) {
        when (state) {
            DmcScannerUiState.Scanning -> Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painterResource(R.drawable.ic_qr_code), contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.sync_connection_scanner_sheet_info_text),
                    style = types.caption1.copy(lineHeight = 16.sp),
                    color = colors.shadeBlack0
                )
            }

            DmcScannerUiState.Error -> ScannerMessage(
                R.string.sync_connection_scanner_sheet_error_title,
                R.string.sync_connection_scanner_sheet_error_text
            )

            DmcScannerUiState.AlreadyConnected -> ScannerMessage(
                R.string.sync_connection_scanner_sheet_already_connected_title,
                R.string.sync_connection_scanner_sheet_already_connected_text
            )

            DmcScannerUiState.Help -> {
                Row(
                    Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.sync_connect_type_button_any_difficulties),
                        style = types.h3,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onCloseHelp) {
                        Icon(
                            painter = painterResource(R.drawable.ic_dialog_close_profile),
                            contentDescription = stringResource(R.string.content_description_close_button),
                            tint = colors.blackBlue
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.sync_dmc_scanner_help),
                    style = types.body1,
                    color = colors.shadeBlack0,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun ScannerMessage(@StringRes title: Int, @StringRes description: Int) {
    val colors = LocalColors.current
    val types = LocalTypes.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(title), style = types.h3, color = colors.black)
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(description),
            style = types.caption1.copy(lineHeight = 16.sp),
            color = colors.shadeBlack0,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(name = "Сканирование DMC", showBackground = true, widthDp = 360, heightDp = 800, locale = "ru")
@Composable
private fun DmcScannerScanningPreview() = DmcScannerPreview(DmcScannerUiState.Scanning)

@Preview(name = "Ошибка чтения", showBackground = true, widthDp = 360, heightDp = 800, locale = "ru")
@Composable
private fun DmcScannerErrorPreview() = DmcScannerPreview(DmcScannerUiState.Error)

@Preview(name = "Глюкометр уже подключён", showBackground = true, widthDp = 360, heightDp = 800, locale = "ru")
@Composable
private fun DmcScannerAlreadyConnectedPreview() = DmcScannerPreview(DmcScannerUiState.AlreadyConnected)

@Preview(name = "Помощь", showBackground = true, widthDp = 360, heightDp = 800, locale = "ru")
@Composable
private fun DmcScannerHelpPreview() = DmcScannerPreview(DmcScannerUiState.Help)

@Composable
private fun DmcScannerPreview(state: DmcScannerUiState) {
    EltaTheme {
        DmcScannerScreen(state, {}, {}, {}, camera = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF282B30), Color(0xFF101114))
                        )
                    )
            )
        })
    }
}
