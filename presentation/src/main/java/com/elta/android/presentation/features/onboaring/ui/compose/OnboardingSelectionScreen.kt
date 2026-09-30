package com.elta.android.presentation.features.onboaring.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.core.compose.widgets.buttons.GradientActionButton
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalTypes

internal data class OnboardingOption(val key: String, val label: String)

private val selectionColor = Color(0xFF34C8A5)

@Composable
internal fun OnboardingSelectionScreen(
    title: String,
    subtitle: String? = null,
    notice: String? = null,
    options: List<OnboardingOption>,
    selectedKey: String?,
    buttonText: String,
    buttonEnabled: Boolean,
    onSelect: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    val types = LocalTypes.current

    BoxWithConstraints(modifier.fillMaxSize().background(colors.white).navigationBarsPadding()) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(start = 16.dp, end = 16.dp, top = 76.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, style = types.h0.copy(fontSize = 28.sp, lineHeight = 28.sp), color = colors.black)
                if (subtitle != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(subtitle, style = types.body1.copy(lineHeight = 18.sp), color = colors.shadeBlack0)
                }
                if (notice != null) {
                    Spacer(Modifier.height(24.dp))
                    Row(
                        Modifier.fillMaxWidth().background(Color(0xFFE5F8F9), RoundedCornerShape(12.dp)).padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            "i",
                            style = types.caption1,
                            color = colors.white,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.size(20.dp).background(Color(0xFF39C7C8), CircleShape).padding(top = 2.dp)
                        )
                        Text(
                            notice,
                            style = types.caption1.copy(lineHeight = 16.sp),
                            color = colors.shadeBlack0,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }

            Column(Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, option ->
                    if (index > 0) Spacer(Modifier.height(8.dp))
                    SelectionOption(
                        label = option.label,
                        selected = selectedKey == option.key,
                        onClick = { onSelect(option.key) }
                    )
                }
                Spacer(Modifier.height(24.dp))
                GradientActionButton(
                    text = buttonText,
                    enabled = buttonEnabled,
                    isLoading = false,
                    shape = 8,
                    onClick = onContinue
                )
            }
        }
    }
}

@Composable
private fun SelectionOption(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalColors.current
    val shape = RoundedCornerShape(8.dp)
    Text(
        text = label,
        style = LocalTypes.current.body1,
        color = colors.shadeBlack0,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .border(if (selected) 2.dp else 1.dp, if (selected) selectionColor else Color(0xFFE4E6E8), shape)
            .clickable(onClick = onClick)
            .padding(top = 12.dp)
    )
}
