package com.elta.android.presentation.features.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.core.compose.widgets.buttons.GradientActionButton
import com.elta.android.presentation.theme.LocalColors

@Composable
internal fun AuthEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    enabled: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done,
    autofillContentType: ContentType = ContentType.EmailAddress,
    maxLength: Int = Int.MAX_VALUE
) {
    val colors = LocalColors.current

    var isFocused by remember {
        mutableStateOf(false)
    }
    var fieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    LaunchedEffect(value) {
        if (fieldValue.text != value) {
            fieldValue = TextFieldValue(value, TextRange(value.length))
        }
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
            value = fieldValue,
            onValueChange = {
                val updated = if (it.text.length > maxLength) {
                    val text = it.text.take(maxLength)
                    it.copy(text = text, selection = TextRange(text.length))
                } else {
                    it
                }
                fieldValue = updated
                if (updated.text != value) onValueChange(updated.text)
            },
            enabled = enabled,
            singleLine = true,
            textStyle = AuthTypography.field.copy(
                color = colors.blackBlue
            ),
            cursorBrush = SolidColor(colors.gGreenB),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() },
                onNext = { onSubmit() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentType = autofillContentType }
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
                            .padding(top = 12.dp, bottom = 12.dp)
                            .heightIn(min = 24.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = label,
                                style = AuthTypography.field,
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
                style = AuthTypography.helper,
                color = colors.red,
                modifier = Modifier.padding(top = 7.dp)
            )
        } else {
            Spacer(Modifier.heightIn(min = 7.dp))
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
    GradientActionButton(
        text = text,
        enabled = enabled,
        isLoading = isLoading,
        shape = shape,
        onClick = onClick,
        modifier = modifier,
        textStyle = AuthTypography.button
    )
}
