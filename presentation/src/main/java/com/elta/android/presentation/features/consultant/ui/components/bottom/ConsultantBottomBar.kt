package com.elta.android.presentation.features.consultant.ui.components.bottom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.disableClickable
import com.elta.android.presentation.core.compose.widgets.HSpacer
import com.elta.android.presentation.core.compose.widgets.HSpacerMedium
import com.elta.android.presentation.core.compose.widgets.animation.HorizontallyAnimation
import com.elta.android.presentation.core.compose.widgets.animation.VerticallyAnimation
import com.elta.android.presentation.core.compose.widgets.buttons.RoundedButton
import com.elta.android.presentation.features.consultant.model.BottomBarIconState
import com.elta.android.presentation.features.consultant.model.RecordGraphState
import com.elta.android.presentation.features.consultant.model.RecordState
import com.elta.android.presentation.features.consultant.model.toUi
import com.elta.android.presentation.theme.GetLocalProperties
import org.threeten.bp.LocalTime

private const val MESSAGE_MAX_LINES = 10
private const val EDITABLE_MESSAGE_MAX_LINES = 1
private const val BOTTOM_BAR_ANIMATION_DURATION_MILLIS = 500

@Composable
fun ConsultantBottomBar(
    inputValue: String,
    isEditMessage: Boolean,
    messageForEdit: String?,
    rightIconState: BottomBarIconState,
    recordGraphState: RecordGraphState,
    onVoiceIconClick: (RecordState) -> Unit,
    onMessageChange: (String) -> Unit,
    deleteVoiceTrackClick: () -> Unit,
    onFileClick: () -> Unit,
    onRightIconClick: (BottomBarIconState) -> Unit,
    onCloseEditClick: () -> Unit
) {
    val isMessageBottom = recordGraphState.recordState == RecordState.Empty

    GetLocalProperties { _, _, colors, _, _ ->
        AnimatedVisibility(
            visible = isMessageBottom,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colors.shadeBlack4)
            ) {
                VerticallyAnimation(visualState = isEditMessage) {
                    EditableMessage(
                        value = messageForEdit.orEmpty(),
                        onCloseEditClick = onCloseEditClick
                    )
                }
                InputField(
                    inputValue = inputValue,
                    iconState = rightIconState,
                    onInputChanged = onMessageChange,
                    onFileClick = onFileClick,
                    onIconClick = onRightIconClick
                )
            }
        }
        HorizontallyAnimation(
            visualState = !isMessageBottom,
            duration = BOTTOM_BAR_ANIMATION_DURATION_MILLIS
        ) {
            WaveRecordGraph(
                recordGraphState = recordGraphState,
                deleteVoiceTrackClick = deleteVoiceTrackClick,
                onVoiceIconClick = onVoiceIconClick
            )
        }
    }
}

@Composable
private fun EditableMessage(
    value: String,
    onCloseEditClick: () -> Unit
) {
    GetLocalProperties { dimens, _, colors, shapes, styles ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.consultantBottomBarContentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HSpacer(width = 44.dp)
            Box(
                modifier = Modifier
                    .width(dimens.smallestDim)
                    .heightIn(min = 32.dp)
                    .background(
                        color = colors.gGreenB,
                        shape = shapes.round
                    )
            )
            HSpacerMedium()
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.consultant_editable_message),
                    style = styles.textStyle2,
                    color = colors.gGreenB
                )
                Text(
                    text = value,
                    color = colors.shadeBlack1,
                    style = styles.caption1,
                    maxLines = EDITABLE_MESSAGE_MAX_LINES,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_dialog_close_profile),
                contentDescription = null,
                modifier = Modifier
                    .clickable(onClick = onCloseEditClick)
                    .padding(start = 22.dp)
            )
        }
    }
}

@Composable
private fun InputField(
    inputValue: String,
    iconState: BottomBarIconState,
    onInputChanged: (String) -> Unit,
    onFileClick: () -> Unit,
    onIconClick: (BottomBarIconState) -> Unit
) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = colors.shadeBlack4)
                .padding(dimens.consultantBottomBarContentPadding)
                // todo: этот паддинг глючит верстку
                .imePadding(),
            horizontalArrangement = Arrangement.spacedBy(dimens.smallDim)
        ) {
            FileButton(onFileClick = onFileClick)
            MessageField(
                inputValue = inputValue,
                onInputChanged = onInputChanged,
                modifier = Modifier.weight(1f)
            )
            SendButton(
                iconState = iconState,
                onIconClick = onIconClick
            )
        }
    }
}

@Composable
private fun WaveRecordGraph(
    recordGraphState: RecordGraphState,
    deleteVoiceTrackClick: () -> Unit,
    onVoiceIconClick: (RecordState) -> Unit
) {
    GetLocalProperties { dimens, _, colors, _, _ ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .disableClickable()
                .background(color = colors.gGreenB)
                .padding(dimens.consultantBottomBarContentPadding),
            horizontalArrangement = Arrangement.spacedBy(dimens.halfMediumDim),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RecordDeleteButton(
                isComplete = recordGraphState.recordState == RecordState.Empty,
                deleteVoiceTrackClick = deleteVoiceTrackClick
            )
            GraphField(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Bottom),
                recordGraphState = recordGraphState
            )
            Duration(recordGraphState.duration.toUi())
            RecordControlButton(
                recordState = recordGraphState.recordState,
                onVoiceIconClick = onVoiceIconClick
            )
        }
    }
}

@Composable
private fun RecordControlButton(
    recordState: RecordState,
    onVoiceIconClick: (RecordState) -> Unit
) {
    val icon = when (recordState) {
        RecordState.Recording -> R.drawable.ic_record_stop
        else -> R.drawable.ic_send
    }
    GetLocalProperties { _, _, colors, _, _ ->
        RoundedButton(
            icon = icon,
            background = colors.white,
            border = null,
            iconColor = colors.gGreenB,
            onClick = { onVoiceIconClick(recordState) }
        )

    }
}

@Composable
private fun RecordDeleteButton(
    isComplete: Boolean,
    deleteVoiceTrackClick: () -> Unit
) {
    GetLocalProperties { _, _, colors, _, _ ->
        val iconColor by animateColorAsState(
            targetValue = if (isComplete) colors.gOrangeB else colors.white,
            label = ""
        )
        val backgroundColor by animateColorAsState(
            targetValue = if (isComplete) colors.white else colors.blackBlue20,
            label = ""
        )
        RoundedButton(
            icon = R.drawable.ic_record_delete,
            background = backgroundColor,
            iconColor = iconColor,
            border = null,
            onClick = deleteVoiceTrackClick
        )
    }
}

@Composable
private fun SendButton(
    iconState: BottomBarIconState,
    onIconClick: (BottomBarIconState) -> Unit
) {
    val icon = when (iconState) {
        BottomBarIconState.SendMessage -> R.drawable.ic_send
        BottomBarIconState.StartRecord -> R.drawable.ic_voice_message
        BottomBarIconState.SaveEdit -> R.drawable.ic_save_edit
    }

    GetLocalProperties { _, _, colors, _, _ ->
        RoundedButton(
            icon = icon,
            background = colors.gGreenB,
            border = null,
            iconColor = colors.shadeBlack4,
            onClick = { onIconClick(iconState) }
        )
    }
}

@Composable
private fun FileButton(onFileClick: () -> Unit) {
    RoundedButton(
        icon = R.drawable.ic_file,
        onClick = onFileClick
    )
}

@Composable
private fun GraphField(
    modifier: Modifier = Modifier,
    recordGraphState: RecordGraphState
) {
    val listState = rememberLazyListState()
    LaunchedEffect(key1 = recordGraphState.recordGraph.size) {
        listState.animateScrollToItem(recordGraphState.recordGraph.size)
    }
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        LazyRow(
            userScrollEnabled = false,
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(dimens.verySmallDim),
            verticalAlignment = Alignment.Bottom,
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = dimens.halfMediumDim)
        ) {
            items(items = recordGraphState.recordGraph) {
                Box(
                    modifier = Modifier
                        .width(dimens.graphItemHeight)
                        .height(dimens.graphItemMaxWidth * it)
                        .background(color = colors.white, shape = shapes.round)
                )
            }
        }

    }
}

@Composable
private fun Duration(time: String) {
    GetLocalProperties { _, _, colors, _, types ->
        Text(
            text = time,
            style = types.title3,
            color = colors.white,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MessageField(
    inputValue: String,
    modifier: Modifier = Modifier,
    onInputChanged: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val visualTransformation = remember { VisualTransformation.None }
    var textField by remember { mutableStateOf("") }
    if (inputValue != textField) textField = inputValue
    GetLocalProperties { dimens, _, colors, shapes, _ ->
        Box(modifier = modifier) {
            BasicTextField(
                value = textField,
                onValueChange = {
                    textField = it
                    onInputChanged(it)
                                },
                interactionSource = interactionSource,
                maxLines = MESSAGE_MAX_LINES,
                visualTransformation = visualTransformation,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colors.white, shape = shapes.consultantTextField)
                    .border(
                        color = colors.shadeBlack3,
                        shape = shapes.consultantTextField,
                        width = dimens.borderWidth
                    )
            ) { innerTextField ->
                TextFieldDefaults.TextFieldDecorationBox(
                    value = inputValue,
                    innerTextField = innerTextField,
                    enabled = true,
                    interactionSource = interactionSource,
                    visualTransformation = visualTransformation,
                    singleLine = false,
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = colors.blackBlue,
                        backgroundColor = colors.white,
                        cursorColor = colors.blackBlue,
                        errorIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        placeholderColor = colors.shadeBlack1
                    ),
                    placeholder = { Text(text = stringResource(id = R.string.consultant_message_placeholder)) },
                    contentPadding = dimens.sendMessageTextFieldPadding
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewConsultantBottomBar() {
    ConsultantBottomBar(
        inputValue = "",
        rightIconState = BottomBarIconState.SendMessage,
        messageForEdit = "Message for edit and very long text for example for check at end o",
        recordGraphState = RecordGraphState(RecordState.Empty, emptyList(), LocalTime.MIN),
        isEditMessage = false,
        onVoiceIconClick = {},
        onMessageChange = {},
        deleteVoiceTrackClick = {},
        onFileClick = {},
        onRightIconClick = {},
        onCloseEditClick = {}
    )
}
