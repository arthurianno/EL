package com.elta.android.presentation.features.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.theme.LocalBrash
import com.elta.android.presentation.theme.LocalColors
import com.elta.android.presentation.theme.LocalDimens
import com.elta.android.presentation.theme.LocalTypes

@Composable
internal fun AuthEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    enabled: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current
    val types = LocalTypes.current

    var isFocused by remember {
        mutableStateOf(false)
    }

    val lineColor = when {
        error != null -> colors.red
        isFocused -> colors.gGreenB
        else -> colors.shadeBlack3
    }

    Column(
        modifier = modifier
    ) {

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = types.body1.copy(
                color = colors.blackBlue
            ),
            cursorBrush = SolidColor(colors.gGreenB),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    isFocused = it.isFocused
                },
            decorationBox = { innerTextField ->

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 0.dp,
                                end = 0.dp,
                                bottom = 8.dp
                            )
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = label,
                                style = types.body1,
                                color = colors.shadeBlack2
                            )
                        }

                        innerTextField()
                    }

                    Divider(
                        modifier = Modifier.fillMaxWidth(),
                        color = lineColor,
                        thickness = 1.dp
                    )
                }
            }
        )

        if (error != null) {
            Text(
                text = error,
                style = types.descriptionError,
                color = colors.red,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
@Composable
internal fun AuthSubmitButton(
    text: String,
    enabled: Boolean,
    isLoading: Boolean,
    shape: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalColors.current

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(shape.dp),
        elevation = null,
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 12.dp
        ),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            disabledBackgroundColor = Color.Transparent,
            contentColor = colors.white,
            disabledContentColor = if (isLoading) colors.white else colors.shadeBlack1
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = LocalDimens.current.downButtonHeight)
            .then(
                if (enabled || isLoading) {
                    Modifier.background(
                        brush = LocalBrash.current.downButton,
                        shape = RoundedCornerShape(shape.dp)
                    )
                } else {
                    Modifier.background(
                        color = colors.shadeBlack3,
                        shape = RoundedCornerShape(shape.dp)
                    )
                }
            )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                Modifier.size(24.dp),
                color = colors.white,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text,
                style = LocalTypes.current.buttonLargeText
            )
        }
    }
}
