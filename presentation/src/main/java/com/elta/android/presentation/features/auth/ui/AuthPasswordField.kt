package com.elta.android.presentation.features.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.R
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalTypes

@Composable
internal fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    label: String,
    helperText: String,
    errorText: String?,
    enabled: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    val types = LocalTypes.current
    var isFocused by remember { mutableStateOf(false) }
    val lineColor = when {
        errorText != null -> colors.red
        isFocused -> colors.gGreenB
        else -> colors.shadeBlack3
    }

    Column(modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = types.body1.copy(color = colors.blackBlue),
            cursorBrush = SolidColor(colors.gGreenB),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            modifier = Modifier.fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
                .semantics {
                    contentDescription = label
                    if (errorText != null) error(errorText)
                },
            decorationBox = { innerTextField ->
                Column {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f).padding(vertical = 8.dp)) {
                            if (value.isEmpty()) {
                                Text(label, style = types.body1, color = colors.shadeBlack2)
                            }
                            innerTextField()
                        }
                        IconButton(onClick = onToggleVisibility, enabled = enabled) {
                            Icon(
                                painter = painterResource(
                                    if (isPasswordVisible) R.drawable.ic_password_hide else R.drawable.ic_show_password
                                ),
                                contentDescription = stringResource(
                                    if (isPasswordVisible) R.string.auth_hide_password else R.string.auth_show_password
                                ),
                                tint = colors.shadeBlack2,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Divider(color = lineColor, thickness = 1.dp)
                }
            }
        )
        Text(
            text = errorText ?: helperText,
            style = if (errorText != null) types.descriptionError else types.caption1,
            color = if (errorText != null) colors.red else colors.shadeBlack2,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
