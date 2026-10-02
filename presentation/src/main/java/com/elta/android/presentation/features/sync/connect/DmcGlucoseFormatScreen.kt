package com.elta.android.presentation.features.sync.connect

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.buttons.GradientActionButton
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalTypes

private val formatAccent = Color(0xFF3EC1C5)
private val unselectedBorderColor = Color(0xFFE3E3E3)
private val unselectedTextColor = Color(0xFF626A7C)
private val selectedTextColor = Color(0xFF17191F)
private val infoBoxBackground = Color(0x213EC1C5)

@Composable
internal fun DmcGlucoseFormatScreen(
    selected: GlucoseFormat?,
    canSave: Boolean,
    isSaving: Boolean,
    onSelect: (GlucoseFormat) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val colors = LocalColors.current
    val types = LocalTypes.current

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(colors.white)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                if (onBack != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_back),
                                contentDescription = null,
                                tint = colors.shadeBlack0
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.height(12.dp))
                }

                Text(
                    text = stringResource(R.string.sync_dmc_format_title),
                    style = types.h0.copy(lineHeight = 28.sp),
                    color = colors.black
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.sync_dmc_format_question),
                    style = types.body1.copy(fontSize = 17.sp, lineHeight = 21.sp),
                    color = Color(0xFFA617191F)
                )
                Spacer(Modifier.height(28.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(infoBoxBackground, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info_fill),
                        contentDescription = null,
                        tint = formatAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.sync_dmc_format_hint),
                        style = types.caption1.copy(fontSize = 14.sp, lineHeight = 20.sp),
                        color = unselectedTextColor,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                ) {
                    DmcFormatOption(
                        text = R.string.profile_glucose_format_plasma,
                        selected = selected == GlucoseFormat.PLASMA,
                        onClick = { onSelect(GlucoseFormat.PLASMA) }
                    )
                    Spacer(Modifier.height(8.dp))
                    DmcFormatOption(
                        text = R.string.profile_glucose_format_caplilary,
                        selected = selected == GlucoseFormat.CAPILLARY,
                        onClick = { onSelect(GlucoseFormat.CAPILLARY) }
                    )
                }
                Spacer(Modifier.height(24.dp))
                GradientActionButton(
                    text = stringResource(R.string.profile_settings_choose),
                    enabled = canSave,
                    isLoading = isSaving,
                    shape = 10,
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DmcFormatOption(
    @StringRes text: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(Color.White)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) formatAccent else unselectedBorderColor,
                shape = shape
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(text),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) selectedTextColor else unselectedTextColor,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(name = "Единица измерения", showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun DmcFormatUnselectedPreview() {
    EltaTheme {
        DmcGlucoseFormatScreen(null, canSave = false, isSaving = false, onSelect = {}, onSave = {})
    }
}

@Preview(name = "Единица измерения — выбор", showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun DmcFormatSelectedPreview() {
    EltaTheme {
        DmcGlucoseFormatScreen(GlucoseFormat.PLASMA, canSave = true, isSaving = false, onSelect = {}, onSave = {})
    }
}
