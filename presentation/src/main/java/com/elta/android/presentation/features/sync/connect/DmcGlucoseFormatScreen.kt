package com.elta.android.presentation.features.sync.connect

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.buttons.GradientActionButton
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalTypes

private val formatAccent = Color(0xFF39C7C8)

@Composable
internal fun DmcGlucoseFormatScreen(
    selected: GlucoseFormat?,
    canSave: Boolean,
    isSaving: Boolean,
    onSelect: (GlucoseFormat) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    val types = LocalTypes.current
    BoxWithConstraints(modifier.fillMaxSize().background(colors.white).statusBarsPadding()) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.sync_dmc_format_title),
                    style = types.h0.copy(lineHeight = 32.sp),
                    color = colors.black
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.sync_dmc_format_question),
                    style = types.body1,
                    color = colors.shadeBlack0
                )
                Spacer(Modifier.height(28.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE5F8F9), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "i",
                        style = types.caption1,
                        color = colors.white,
                        modifier = Modifier
                            .size(20.dp)
                            .background(formatAccent, CircleShape)
                            .padding(top = 2.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.sync_dmc_format_hint),
                        style = types.caption1.copy(lineHeight = 17.sp),
                        color = colors.shadeBlack0,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            Column(Modifier.fillMaxWidth().selectableGroup()) {
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
                Spacer(Modifier.height(12.dp))
                GradientActionButton(
                    text = stringResource(R.string.profile_settings_choose),
                    enabled = canSave,
                    isLoading = isSaving,
                    shape = 8,
                    onClick = onSave
                )
            }
        }
    }
}

@Composable
private fun DmcFormatOption(@StringRes text: Int, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalColors.current
    val shape = RoundedCornerShape(8.dp)
    Text(
        text = stringResource(text),
        style = LocalTypes.current.body1,
        color = colors.shadeBlack0,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(if (selected) 2.dp else 1.dp, if (selected) formatAccent else Color(0xFFE4E6E8), shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(start = 16.dp, top = 13.dp)
    )
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
