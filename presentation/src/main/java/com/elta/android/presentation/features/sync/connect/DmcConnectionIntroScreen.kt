package com.elta.android.presentation.features.sync.connect

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.buttons.GradientActionButton
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalTypes

@Composable
internal fun DmcConnectionIntroScreen(
    onBack: () -> Unit,
    onScan: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes illustrationRes: Int? = null,
) {
    val colors = LocalColors.current
    val types = LocalTypes.current
    var helpVisible by rememberSaveable { mutableStateOf(false) }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(colors.white)
            .statusBarsPadding()
    ) {
        val horizontalPadding = if (maxWidth < 340.dp) 20.dp else 24.dp
        val illustrationHeight = minOf(maxHeight * 0.62f, 500.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.content_description_back_button),
                            tint = colors.blackBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { helpVisible = true }) {
                        Text(
                            text = stringResource(R.string.sync_connect_type_button_need_help),
                            style = types.caption1,
                            color = colors.shadeBlack1
                        )
                    }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = horizontalPadding, end = horizontalPadding, top = 22.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sync_dmc_intro_title),
                        style = types.h0.copy(lineHeight = 32.sp),
                        color = colors.black
                    )
                    Spacer(Modifier.height(12.dp))
                    DmcInstructionStep("1.", stringResource(R.string.sync_dmc_intro_step_bluetooth))
                    Spacer(Modifier.height(16.dp))
                    DmcInstructionStep("2.", stringResource(R.string.sync_dmc_intro_step_scan))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(illustrationHeight)
            ) {
                if (illustrationRes != null) {
                    Image(
                        painter = painterResource(illustrationRes),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.BottomCenter,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                GradientActionButton(
                    text = stringResource(R.string.sync_how_to_connect_button),
                    enabled = true,
                    isLoading = false,
                    shape = 10,
                    onClick = onScan,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = horizontalPadding, vertical = 16.dp)
                )
            }
        }
    }

    if (helpVisible) {
        AlertDialog(
            onDismissRequest = { helpVisible = false },
            title = { Text(stringResource(R.string.sync_connect_type_button_need_help)) },
            text = { Text(stringResource(R.string.sync_dmc_intro_help)) },
            confirmButton = {
                TextButton(onClick = { helpVisible = false }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }
}

@Composable
private fun DmcInstructionStep(number: String, description: String) {
    val colors = LocalColors.current
    val style = LocalTypes.current.subtitle2.copy(lineHeight = 20.sp)
    Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 24.dp)) {
        Text(number, style = style, color = colors.shadeBlack0, modifier = Modifier.width(20.dp))
        Text(description, style = style, color = colors.shadeBlack0, modifier = Modifier.weight(1f))
    }
}

@Preview(name = "Подключение глюкометра", showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Preview(name = "Компактный экран", showBackground = true, widthDp = 320, heightDp = 480, fontScale = 1.5f, locale = "ru")
@Composable
private fun DmcConnectionIntroPreview() {
    EltaTheme { DmcConnectionIntroPreviewContent() }
}

@Composable
internal fun DmcConnectionIntroPreviewContent() {
    DmcConnectionIntroScreen(onBack = {}, onScan = {})
}
