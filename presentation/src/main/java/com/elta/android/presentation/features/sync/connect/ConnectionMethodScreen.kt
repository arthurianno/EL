package com.elta.android.presentation.features.sync.connect

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalTypes

@Composable
internal fun ConnectionMethodScreen(
    isOnBoarding: Boolean,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onDmc: () -> Unit,
    onPin: () -> Unit,
    onLater: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    val types = LocalTypes.current
    Column(
        modifier.fillMaxSize().background(colors.white).statusBarsPadding().navigationBarsPadding()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp).heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isOnBoarding) {
                IconButton(onClick = onBack) {
                    Icon(
                        painterResource(R.drawable.ic_back),
                        contentDescription = stringResource(R.string.content_description_back_button),
                        tint = colors.blackBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
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
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                stringResource(R.string.sync_connect_method_title),
                style = types.h0.copy(lineHeight = 28.sp),
                color = colors.black
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.sync_connect_method_description),
                style = types.subtitle2.copy(lineHeight = 20.sp),
                color = colors.black.copy(alpha = 0.65f)
            )
            Spacer(Modifier.height(32.dp))
            ConnectionMethodButton(
                title = stringResource(R.string.sync_connect_type_button_scan_dmc),
                description = stringResource(R.string.sync_connect_method_dmc_description),
                icon = R.drawable.ic_dmc_scan_icon,
                onClick = onDmc
            )
            Spacer(Modifier.height(16.dp))
            ConnectionMethodButton(
                title = stringResource(R.string.sync_connect_type_button_enter_pin),
                description = stringResource(R.string.sync_connect_method_pin_description),
                icon = R.drawable.ic_pin_icon,
                onClick = onPin
            )
        }
        if (isOnBoarding) {
            TextButton(
                onClick = onLater,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp).heightIn(min = 52.dp)
            ) {
                Text(stringResource(R.string.sync_connect_later), style = types.buttonLargeText, color = colors.gGreenB)
            }
        }
    }
}

@Composable
private fun ConnectionMethodButton(
    title: String,
    description: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit
) {
    val colors = LocalColors.current
    val types = LocalTypes.current
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, colors.gGreenB.copy(alpha = 0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(backgroundColor = colors.white),
        modifier = Modifier.fillMaxWidth().heightIn(min = 104.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).background(colors.gGreenB10, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(icon), contentDescription = null, tint = colors.gGreenB, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = types.h3.copy(fontSize = 17.sp, lineHeight = 22.sp), color = colors.black)
                Spacer(Modifier.height(4.dp))
                Text(description, style = types.body1.copy(lineHeight = 20.sp), color = colors.shadeBlack0)
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                painterResource(R.drawable.ic_back),
                contentDescription = null,
                tint = colors.gGreenB,
                modifier = Modifier.size(20.dp).rotate(180f)
            )
        }
    }
}

@Preview(name = "Выбор подключения", widthDp = 375, heightDp = 812, showBackground = true, locale = "ru")
@Composable
private fun ConnectionMethodPreview() = EltaTheme {
    ConnectionMethodScreen(true, {}, {}, {}, {}, {})
}
